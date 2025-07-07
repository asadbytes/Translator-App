package com.asadbyte.translatorapp.camera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationApiModule
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.data.room.TranslationHistory
import com.asadbyte.translatorapp.main.TranslatorApplication
import com.asadbyte.translatorapp.utils.ImageOverlayProcessor
import com.asadbyte.translatorapp.utils.TranslatedTextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val dbRepository = (application as TranslatorApplication).repository

    private val translationApiModule = TranslationApiModule()
    private val imageOverlayProcessor = ImageOverlayProcessor() // The class from the previous answer

    val isFlashEnabled = MutableLiveData<Boolean>(false)
    private var lastProcessedUri: String? = null

    // Holds the final results for the next fragment (no changes here)
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _currentTranslation = MutableLiveData<TranslationHistory?>()
    val currentTranslation: LiveData<TranslationHistory?> = _currentTranslation

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> = _translatedText

    // Manages the loading state
    private val _processingState = MutableLiveData<String>()
    val processingState: LiveData<String> = _processingState

    // NEW: We will now hold the bitmaps here
    val originalBitmap = MutableLiveData<Bitmap>()
    val overlaidBitmap = MutableLiveData<Bitmap>()

    // Add this function to toggle the state
    fun toggleFlash() {
        isFlashEnabled.value = !(isFlashEnabled.value ?: false)
    }

    // The main function called by the Fragment
    fun processImage(imageUri: Uri, context: Context, sourceLanguage: String, targetLanguage: String) {
        if (imageUri.toString() == lastProcessedUri) {
            return
        }
        lastProcessedUri = imageUri.toString()



        viewModelScope.launch(Dispatchers.IO) {
            _processingState.postValue("Recognizing text...")
            try {
                // Load the bitmap and immediately post it to LiveData
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                originalBitmap.postValue(bitmap) // <-- ADDED: Save the original bitmap

                val image = InputImage.fromBitmap(bitmap, 0)
                recognizeText(image, sourceLanguage, targetLanguage)
            } catch (e: Exception) {
                _processingState.postValue("Error preparing image: ${e.message}")
                Log.e("CameraViewModel", "Image processing error", e)
            }
        }
    }

    private suspend fun recognizeText(image: InputImage, sourceLanguage: String, targetLanguage: String) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val result = recognizer.process(image).await()

            // ---- JOB 1: Handle full text for the next screen (your existing logic) ----
            val fullExtractedText = result.text
            if (fullExtractedText.isNotBlank()) {
                _recognizedText.postValue(fullExtractedText.trim())
                translateFullText(fullExtractedText.trim(), sourceLanguage, targetLanguage)
            } else {
                _processingState.postValue("No text found.")
                _recognizedText.postValue("")
                _translatedText.postValue("")
            }

            // ---- JOB 2: Handle block-by-block translation for the image overlay ----
            val textBlocks = result.textBlocks
            if (textBlocks.isNotEmpty() && originalBitmap.value != null) {
                // This function will handle translating each block and creating the image
                createOverlayFromBlocks(textBlocks, sourceLanguage, targetLanguage)
            }

        } catch (e: Exception) {
            _processingState.postValue("Text recognition failed: ${e.message}")
            Log.e("CameraViewModel", "ML Kit recognition error", e)
        }
    }

    // RENAMED from translateExtractedText for clarity
    private fun translateFullText(text: String, sourceLanguage: String, targetLanguage: String) {
        if (text == _recognizedText.value && !_translatedText.value.isNullOrEmpty()) {
            _processingState.postValue("done") // Ensure state is updated
            return
        }
        viewModelScope.launch {
            // ... This is your existing translation logic, it works perfectly for Job 1. No changes needed inside.
            _processingState.postValue("Translating...")
            val sourceCode = translationApiModule.getLanguageCode(sourceLanguage)
            val targetCode = translationApiModule.getLanguageCode(targetLanguage)

            when (val result = translationApiModule.translate(text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> {
                    _translatedText.postValue(result.text)

                    val historyRecord = TranslationHistory(
                        originalText = text,
                        translatedText = result.text,
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage
                    )
                    val newId = dbRepository.insert(historyRecord)
                    _currentTranslation.postValue(historyRecord.copy(id = newId.toInt()))

                    _processingState.postValue("done")
                }
                is TranslationResult.Error -> {
                    _processingState.postValue(result.message)
                }
            }
        }
    }

    fun toggleBookmark() {
        // Get the current translation object
        val currentItem = _currentTranslation.value ?: return

        viewModelScope.launch {
            // Create a new object with the bookmark state flipped
            val updatedItem = currentItem.copy(isBookmarked = !currentItem.isBookmarked)

            // Update the database
            dbRepository.update(updatedItem)

            // Update the LiveData so the UI can react to the change
            _currentTranslation.value = updatedItem
        }
    }

    // NEW FUNCTION to handle Job 2
    private suspend fun createOverlayFromBlocks(
        blocks: List<Text.TextBlock>,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        val sourceCode = translationApiModule.getLanguageCode(sourceLanguage)
        val targetCode = translationApiModule.getLanguageCode(targetLanguage)

        // Translate each block of text individually
        val translatedBlocks = blocks.mapNotNull { block ->
            when (val result = translationApiModule.translate(block.text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> TranslatedTextBlock(result.text, block.boundingBox!!)
                is TranslationResult.Error -> null // Skip blocks that fail to translate
            }
        }

        // Now, use the processor to create the new image
        val newBitmap = imageOverlayProcessor.createOverlayBitmap(
            originalBitmap.value!!,
            translatedBlocks
        )
        overlaidBitmap.postValue(newBitmap)
    }

    fun clearLastProcessedUri() {
        lastProcessedUri = null
    }
}

package com.asadbyte.translatorapp.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationRepository
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.main.ImageOverlayProcessor
import com.asadbyte.translatorapp.main.TranslatedTextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CameraViewModel : ViewModel() {
    private val repository = TranslationRepository()
    private val imageOverlayProcessor = ImageOverlayProcessor() // The class from the previous answer

    // Holds the final results for the next fragment (no changes here)
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText
    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> = _translatedText

    // Manages the loading state
    private val _processingState = MutableLiveData<String>()
    val processingState: LiveData<String> = _processingState

    // NEW: We will now hold the bitmaps here
    val originalBitmap = MutableLiveData<Bitmap>()
    val overlaidBitmap = MutableLiveData<Bitmap>()

    // The main function called by the Fragment
    fun processImage(imageUri: Uri, context: Context, sourceLanguage: String, targetLanguage: String) {
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
        viewModelScope.launch {
            // ... This is your existing translation logic, it works perfectly for Job 1. No changes needed inside.
            _processingState.postValue("Translating...")
            val sourceCode = repository.getLanguageCode(sourceLanguage)
            val targetCode = repository.getLanguageCode(targetLanguage)

            when (val result = repository.translate(text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> {
                    _translatedText.postValue(result.text)
                    _processingState.postValue("done")
                }
                is TranslationResult.Error -> {
                    _processingState.postValue(result.message)
                }
            }
        }
    }

    // NEW FUNCTION to handle Job 2
    private suspend fun createOverlayFromBlocks(
        blocks: List<Text.TextBlock>,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        val sourceCode = repository.getLanguageCode(sourceLanguage)
        val targetCode = repository.getLanguageCode(targetLanguage)

        // Translate each block of text individually
        val translatedBlocks = blocks.mapNotNull { block ->
            when (val result = repository.translate(block.text, sourceCode.toString(), targetCode.toString())) {
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
}

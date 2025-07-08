package com.asadbyte.translatorapp.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.asadbyte.translatorapp.data.TranslationApiModule
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.utils.ImageOverlayProcessor
import com.asadbyte.translatorapp.utils.TranslatedTextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ImageTranslateModule {

    private val translationApiModule = TranslationApiModule()
    private val imageOverlayProcessor = ImageOverlayProcessor()

    var recognizedText: MutableLiveData<String> = MutableLiveData("")
    var translatedText: MutableLiveData<String> = MutableLiveData("")
    var processingState: MutableLiveData<String> = MutableLiveData("")

    // Hold the bitmaps here
    val originalBitmap = MutableLiveData<Bitmap>()
    val overlaidBitmap = MutableLiveData<Bitmap>()

    fun processImage(imageUri: Uri, context: Context, sourceLanguage: String, targetLanguage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            processingState.postValue("Recognizing text...")
            try {
                // Load the bitmap and immediately post it to LiveData
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                originalBitmap.postValue(bitmap)

                val image = InputImage.fromBitmap(bitmap, 0)
                recognizeText(image, sourceLanguage, targetLanguage)
            } catch (e: Exception) {
                processingState.postValue("Error preparing image: ${e.message}")
                Log.e("ImageTranslateModule", "Image processing error", e)
            }
        }
    }

    private suspend fun recognizeText(image: InputImage, sourceLanguage: String, targetLanguage: String) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val result = recognizer.process(image).await()

            // JOB 1: Handle full text for the next screen
            val fullExtractedText = result.text
            if (fullExtractedText.isNotBlank()) {
                recognizedText.postValue(fullExtractedText.trim())
                translateFullText(fullExtractedText.trim(), sourceLanguage, targetLanguage)
            } else {
                processingState.postValue("No text found.")
                recognizedText.postValue("")
                translatedText.postValue("")
            }

            // JOB 2: Handle block-by-block translation for the image overlay
            val textBlocks = result.textBlocks
            if (textBlocks.isNotEmpty() && originalBitmap.value != null) {
                createOverlayFromBlocks(textBlocks, sourceLanguage, targetLanguage)
            }

        } catch (e: Exception) {
            processingState.postValue("Text recognition failed: ${e.message}")
            Log.e("ImageTranslateModule", "ML Kit recognition error", e)
        }
    }

    private suspend fun translateFullText(text: String, sourceLanguage: String, targetLanguage: String) {
        if (text == recognizedText.value && !translatedText.value.isNullOrEmpty()) {
            processingState.postValue("done")
            return
        }

        try {
            processingState.postValue("Translating...")
            val sourceCode = translationApiModule.getLanguageCode(sourceLanguage)
            val targetCode = translationApiModule.getLanguageCode(targetLanguage)

            when (val result = translationApiModule.translate(text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> {
                    translatedText.postValue(result.text)
                    processingState.postValue("done")
                }
                is TranslationResult.Error -> {
                    processingState.postValue(result.message)
                }
            }
        } catch (e: Exception) {
            processingState.postValue("Translation failed: ${e.message}")
            Log.e("ImageTranslateModule", "Translation error", e)
        }
    }

    private suspend fun createOverlayFromBlocks(
        blocks: List<Text.TextBlock>,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        try {
            val sourceCode = translationApiModule.getLanguageCode(sourceLanguage)
            val targetCode = translationApiModule.getLanguageCode(targetLanguage)

            // Translate each block of text individually
            val translatedBlocks = blocks.mapNotNull { block ->
                try {
                    when (val result = translationApiModule.translate(block.text, sourceCode.toString(), targetCode.toString())) {
                        is TranslationResult.Success -> TranslatedTextBlock(result.text, block.boundingBox!!)
                        is TranslationResult.Error -> {
                            Log.e("ImageTranslateModule", "Translation failed for block: ${block.text}, error: ${result.message}")
                            null
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageTranslateModule", "Exception translating block: ${block.text}", e)
                    null
                }
            }

            // Create the overlay bitmap
            if (translatedBlocks.isNotEmpty()) {
                val newBitmap = imageOverlayProcessor.createOverlayBitmap(
                    originalBitmap.value!!,
                    translatedBlocks
                )
                overlaidBitmap.postValue(newBitmap)
            }
        } catch (e: Exception) {
            Log.e("ImageTranslateModule", "Error creating overlay from blocks", e)
        }
    }

    // NEW: Public method for service to use - returns String directly
    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String? {
        return try {
            val sourceCode = translationApiModule.getLanguageCode(sourceLanguage)
            val targetCode = translationApiModule.getLanguageCode(targetLanguage)

            when (val result = translationApiModule.translate(text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> result.text
                is TranslationResult.Error -> {
                    Log.e("ImageTranslateModule", "Translation failed: ${result.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ImageTranslateModule", "Translation exception", e)
            null
        }
    }
}

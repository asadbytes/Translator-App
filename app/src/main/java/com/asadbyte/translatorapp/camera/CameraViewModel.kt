package com.asadbyte.translatorapp.camera

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationRepository
import com.asadbyte.translatorapp.data.TranslationResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CameraViewModel : ViewModel() {

    private val repository = TranslationRepository()

    // Holds the final results
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> = _translatedText

    // Manages the loading state
    private val _processingState = MutableLiveData<String>()
    val processingState: LiveData<String> = _processingState

    // The main function called by the Fragment
    fun processImage(
        imageUri: Uri,
        context: Context,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _processingState.postValue("Recognizing text...")
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizeText(image, sourceLanguage, targetLanguage)
            } catch (e: Exception) {
                _processingState.postValue("Error preparing image: ${e.message}")
                Log.e("CameraViewModel", "Image processing error", e)
            }
        }
    }

    private suspend fun recognizeText(
        image: InputImage,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            // 2. Process the image
            val result = recognizer.process(image).await()
            val extractedText = result.text

            if (extractedText.isNotBlank()) {
                Log.d("CameraViewModel", "ML Kit recognized text: $extractedText")
                _recognizedText.postValue(extractedText.trim())
                translateExtractedText(extractedText.trim(), sourceLanguage, targetLanguage)
            } else {
                _processingState.postValue("No text could be recognized.")
                _recognizedText.postValue("")
                _translatedText.postValue("")
            }
        } catch (e: Exception) {
            _processingState.postValue("Text recognition failed: ${e.message}")
            Log.e("CameraViewModel", "ML Kit recognition error", e)
        } finally {
            recognizer.close()
        }
    }

    private fun translateExtractedText(text: String, sourceLanguage: String, targetLanguage: String) {
        if (text.isBlank()) {
            _processingState.postValue("No text found to translate.")
            _translatedText.postValue("")
            return
        }

        viewModelScope.launch {
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
}
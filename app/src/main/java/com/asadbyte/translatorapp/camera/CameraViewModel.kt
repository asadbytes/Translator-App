package com.asadbyte.translatorapp.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationRepository
import com.asadbyte.translatorapp.data.TranslationResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val repository = TranslationRepository()

    // Holds the final results
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> = _translatedText

    // Manages the loading state (e.g., "Recognizing...", "Translating...")
    private val _processingState = MutableLiveData<String>()
    val processingState: LiveData<String> = _processingState

    // The main function called by the Fragment
    fun processImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _processingState.value = "Recognizing text..."
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        _recognizedText.value = extractedText

                        // Once text is extracted, start translating it
                        translateExtractedText(extractedText)
                    }
                    .addOnFailureListener { e ->
                        _processingState.value = "Text recognition failed: ${e.message}"
                    }

            } catch (e: Exception) {
                _processingState.value = "Error preparing image: ${e.message}"
            }
        }
    }

    private fun translateExtractedText(text: String) {
        if (text.isBlank()) {
            _processingState.value = "No text found to translate."
            _translatedText.value = "" // Set to empty
            return
        }

        viewModelScope.launch {
            _processingState.value = "Translating..."
            // Assuming default translation from English to Urdu for this feature
            // You can make this dynamic if needed
            val sourceCode = repository.getLanguageCode("English")
            val targetCode = repository.getLanguageCode("Urdu")

            when (val result = repository.translate(text, sourceCode.toString(), targetCode.toString())) {
                is TranslationResult.Success -> {
                    _translatedText.value = result.text
                    _processingState.value = "done"
                }
                is TranslationResult.Error -> {
                    _processingState.value = result.message
                }
            }
        }
    }
}
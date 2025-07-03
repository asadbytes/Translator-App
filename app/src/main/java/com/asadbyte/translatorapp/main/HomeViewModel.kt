package com.asadbyte.translatorapp.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

val homeViewModelTag = "HomeViewModel"
class HomeViewModel : ViewModel() {

    // For Source Language
    private val _sourceLanguage = MutableLiveData<String>("English") // Set a default value
    val sourceLanguage: LiveData<String> = _sourceLanguage

    // For Target Language
    private val _targetLanguage = MutableLiveData<String>("Urdu") // Set a default value
    val targetLanguage: LiveData<String> = _targetLanguage

    private val _translatedText = MutableLiveData<Event<String>>()
    val translatedText: LiveData<Event<String>> = _translatedText

    private val _translationError = MutableLiveData<String>()
    val translationError: LiveData<String> = _translationError

    private val _translationState = MutableLiveData<String>()
    val translationState: LiveData<String> = _translationState

    private var translator: Translator? = null

    private fun getLanguageCode(languageName: String): String? {
        return when (languageName) {
            "English" -> TranslateLanguage.ENGLISH
            "Spanish" -> TranslateLanguage.SPANISH
            "French" -> TranslateLanguage.FRENCH
            "German" -> TranslateLanguage.GERMAN
            "Italian" -> TranslateLanguage.ITALIAN
            "Portuguese" -> TranslateLanguage.PORTUGUESE
            "Russian" -> TranslateLanguage.RUSSIAN
            "Chinese" -> TranslateLanguage.CHINESE
            "Japanese" -> TranslateLanguage.JAPANESE
            "Korean" -> TranslateLanguage.KOREAN
            "Arabic" -> TranslateLanguage.ARABIC
            "Hindi" -> TranslateLanguage.HINDI
            "Urdu" -> TranslateLanguage.URDU
            else -> null // Language not supported
        }
    }

    fun translate(textToTranslate: String) {
        _translationState.value = "Downloading languages..."
        val sourceLangCode = getLanguageCode(sourceLanguage.value ?: "")
        val targetLangCode = getLanguageCode(targetLanguage.value ?: "")

        if (sourceLangCode == null || targetLangCode == null) {
            _translationError.value = "Language not supported."
            return
        }

        downloadModelsAndTranslate(textToTranslate, sourceLangCode, targetLangCode)
    }

    private fun downloadModelsAndTranslate(textToTranslate: String, sourceLangCode: String, targetLangCode: String) {
        val modelManager = RemoteModelManager.getInstance()

        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                val modelCodes = models.joinToString(separator = ", ") { it.language }
                Log.d(homeViewModelTag, "Models currently on device: [$modelCodes]")
            }
        Log.d(homeViewModelTag, "Submitting parallel download task for ($sourceLangCode) and ($targetLangCode)...")

        val sourceModel = TranslateRemoteModel.Builder(sourceLangCode).build()
        val targetModel = TranslateRemoteModel.Builder(targetLangCode).build()
        val conditions = DownloadConditions.Builder().build()
        // Use Tasks.whenAllSuccess to handle both downloads together
        val downloadSourceTask = modelManager.download(sourceModel, conditions)
        val downloadTargetTask = modelManager.download(targetModel, conditions)

        Tasks.whenAllSuccess<Void>(downloadSourceTask, downloadTargetTask)
            .addOnSuccessListener {
                Log.d(homeViewModelTag, "Both models are ready.")
                _translationState.value = "Translating..." // <-- Update state
                createTranslatorAndPerformTranslation(textToTranslate, sourceLangCode, targetLangCode)
            }
            .addOnFailureListener { exception ->
                Log.e(homeViewModelTag, "Model download failed:", exception)
                _translationError.value = "Failed to download language: ${exception.message}"
                _translationState.value = "error" // <-- Clear state on failure
            }
    }

    private fun createTranslatorAndPerformTranslation(text: String, sourceLangCode: String, targetLangCode: String) {
        Log.d(homeViewModelTag, "Both models ready. Creating translator.")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        translator = Translation.getClient(options)

        Log.d(homeViewModelTag, "Performing translation...")
        translator!!.translate(text)
            .addOnSuccessListener { translatedResult ->
                Log.d(homeViewModelTag, "Translation successful: $translatedResult")
                _translatedText.value = Event(translatedResult)
                _translationState.value = "done"
            }
            .addOnFailureListener { exception ->
                Log.e(homeViewModelTag, "Translation failed:", exception)
                _translationError.value = "Translation failed: ${exception.message}"
                _translationState.value = "error" // <-- Clear state on failure
            }
    }

    fun updateSourceLanguage(language: String) {
        Log.d(homeViewModelTag, "updateSourceLanguage: $language")
        _sourceLanguage.value = language
    }

    fun updateTargetLanguage(language: String) {
        Log.d(homeViewModelTag, "updateTargetLanguage: $language")
        _targetLanguage.value = language
    }

    override fun onCleared() {
        Log.d(homeViewModelTag, "onCleared")
        super.onCleared()
        translator?.close()
    }

    fun swapLanguages() {
        Log.d(homeViewModelTag, "swapLanguages: ${sourceLanguage.value} ${targetLanguage.value}")
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
    }
}


open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
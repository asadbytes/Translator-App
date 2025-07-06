package com.asadbyte.translatorapp.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationApiModule
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.data.room.TranslationHistory
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.launch

val homeViewModelTag = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val translationApiModule = TranslationApiModule()
    private val dbRepository = (application as TranslatorApplication).repository

    private val _sourceLanguage = MutableLiveData<String>("English") // Set a default value
    val sourceLanguage: LiveData<String> = _sourceLanguage

    private val _targetLanguage = MutableLiveData<String>("Urdu") // Set a default value
    val targetLanguage: LiveData<String> = _targetLanguage

    // Holds the translation result for the UI to observe
    private val _translationResult = MutableLiveData<Event<TranslationResult>>()
    val translationResult: LiveData<Event<TranslationResult>> = _translationResult

    private val _translationState = MutableLiveData<String>()
    val translationState: LiveData<String> = _translationState

    private var translator: Translator? = null



    fun translate(text: String) {
        viewModelScope.launch {
            _translationState.value = "Translating..."

            try {
                val sourceCode = translationApiModule.getLanguageCode(sourceLanguage.value!!) // Assumes value is not null
                val targetCode = translationApiModule.getLanguageCode(targetLanguage.value!!)
                val result = translationApiModule.translate(text, sourceCode.toString(), targetCode.toString())

                if (result is TranslationResult.Success) {
                    // --- SAVE TO DATABASE ---
                    // Create a history object from the successful translation
                    val historyRecord = TranslationHistory(
                        originalText = text,
                        translatedText = result.text,
                        sourceLanguage = sourceLanguage.value!!,
                        targetLanguage = targetLanguage.value!!
                    )
                    // Insert it into the database
                    dbRepository.insert(historyRecord)
                }

                _translationResult.value = Event(result)
            } catch (e: Exception) {
                _translationState.value = TranslationResult.Error(e.message.toString()).toString()
            } finally {
                _translationState.value = "done"
            }
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

    fun getLocaleForSpeech(languageName: String): String {
        return translationApiModule.getLocaleForSpeech(languageName)
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
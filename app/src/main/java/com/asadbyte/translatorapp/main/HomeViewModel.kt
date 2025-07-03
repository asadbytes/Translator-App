package com.asadbyte.translatorapp.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationRepository
import com.asadbyte.translatorapp.data.TranslationResult
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch

val homeViewModelTag = "HomeViewModel"

class HomeViewModel : ViewModel() {

    private val repository = TranslationRepository()

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
                val sourceCode = repository.getLanguageCode(sourceLanguage.value!!) // Assumes value is not null
                val targetCode = repository.getLanguageCode(targetLanguage.value!!)
                val result = repository.translate(text, sourceCode.toString(), targetCode.toString())
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
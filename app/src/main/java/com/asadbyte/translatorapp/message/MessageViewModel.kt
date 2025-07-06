package com.asadbyte.translatorapp.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asadbyte.translatorapp.data.TranslationApiModule
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.main.Event
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    private val repository = TranslationApiModule()

    // Holds the selected languages for the message screen
    private val _sourceLanguage = MutableLiveData("English")
    val sourceLanguage: LiveData<String> = _sourceLanguage

    private val _targetLanguage = MutableLiveData("Urdu")
    val targetLanguage: LiveData<String> = _targetLanguage

    // Holds the translation result and the destination user flag
    // The boolean is 'true' if the translated text is for user 2, 'false' if for user 1.
    private val _translationResult = MutableLiveData<Event<Pair<TranslationResult, Boolean>>>()
    val translationResult: LiveData<Event<Pair<TranslationResult, Boolean>>> = _translationResult

    // Manages the loading state (e.g., "Translating...")
    private val _translationState = MutableLiveData<String>()
    val translationState: LiveData<String> = _translationState

    fun translateMessage(text: String, reverseDirection: Boolean) {
        viewModelScope.launch {
            _translationState.value = "Translating..."
            try {
                val sourceLang = if (reverseDirection) targetLanguage.value!! else sourceLanguage.value!!
                val targetLang = if (reverseDirection) sourceLanguage.value!! else targetLanguage.value!!
                val sourceCode = repository.getLanguageCode(sourceLang)
                val targetCode = repository.getLanguageCode(targetLang)

                val result = repository.translate(text, sourceCode.toString(), targetCode.toString())

                // The destination user is the opposite of the source direction
                val isTranslationForUser2 = !reverseDirection
                _translationResult.value = Event(Pair(result, isTranslationForUser2))

            } finally {
                _translationState.value = "done"
            }
        }
    }

    fun getLanguageCode(languageName: String): String {
        return repository.getLanguageCode(languageName).toString()
    }


    fun getLocaleForSpeech(languageName: String): String {
        return repository.getLocaleForSpeech(languageName)
    }

    // Functions to update languages
    fun updateSourceLanguage(name: String) { _sourceLanguage.value = name }
    fun updateTargetLanguage(name: String) { _targetLanguage.value = name }
    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
    }
}
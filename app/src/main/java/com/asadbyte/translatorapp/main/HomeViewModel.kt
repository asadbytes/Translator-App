package com.asadbyte.translatorapp.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // For Source Language
    private val _sourceLanguage = MutableLiveData<String>("English") // Set a default value
    val sourceLanguage: LiveData<String> = _sourceLanguage

    // For Target Language
    private val _targetLanguage = MutableLiveData<String>("Urdu") // Set a default value
    val targetLanguage: LiveData<String> = _targetLanguage


    fun updateSourceLanguage(language: String) {
        _sourceLanguage.value = language
    }

    fun updateTargetLanguage(language: String) {
        _targetLanguage.value = language
    }
}
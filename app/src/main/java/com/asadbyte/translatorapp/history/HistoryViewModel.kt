package com.asadbyte.translatorapp.history

import android.app.Application
import androidx.lifecycle.*
import com.asadbyte.translatorapp.data.room.TranslationHistory
import com.asadbyte.translatorapp.main.TranslatorApplication
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // Get a reference to the repository from your Application class
    private val repository = (application as TranslatorApplication).repository

    // Use LiveData to expose the Flow from the repository to the UI
    val allHistory: LiveData<List<TranslationHistory>> = repository.allHistory.asLiveData()

    // Example function to insert data, called from a fragment
    fun insert(translation: TranslationHistory) = viewModelScope.launch {
        repository.insert(translation)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }
}
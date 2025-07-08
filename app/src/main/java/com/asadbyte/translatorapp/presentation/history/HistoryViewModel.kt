package com.asadbyte.translatorapp.presentation.history

import android.app.Application
import androidx.lifecycle.*
import com.asadbyte.translatorapp.data.room.TranslationHistory
import com.asadbyte.translatorapp.presentation.main.TranslatorApplication
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // Get a reference to the repository from your Application class
    private val repository = (application as TranslatorApplication).repository

    // Use LiveData to expose the Flow from the repository to the UI
    val allHistory: LiveData<List<TranslationHistory>> = repository.allHistory.asLiveData()

    private val _isSelectionModeActive = MutableLiveData<Boolean>(false)
    val isSelectionModeActive: LiveData<Boolean> = _isSelectionModeActive

    private val _selectedItems = MutableLiveData<Set<Int>>(emptySet())
    val selectedItems: LiveData<Set<Int>> = _selectedItems

    fun toggleSelection(itemId: Int) {
        val currentSelection = _selectedItems.value ?: emptySet()
        _selectedItems.value = if (currentSelection.contains(itemId)) {
            currentSelection - itemId // Remove if already selected
        } else {
            currentSelection + itemId // Add if not selected
        }
        // If the last item is deselected, exit selection mode
        if (_selectedItems.value.isNullOrEmpty()) {
            disableSelectionMode()
        }
    }

    fun enableSelectionMode() {
        _isSelectionModeActive.value = true
    }

    fun disableSelectionMode() {
        _isSelectionModeActive.value = false
        _selectedItems.value = emptySet() // Clear selection
    }

    fun selectAll() {
        val allIds = allHistory.value?.map { it.id }?.toSet() ?: emptySet()
        _selectedItems.value = allIds
    }

    fun deleteSelectedItems() = viewModelScope.launch {
        val itemsToDelete = _selectedItems.value ?: return@launch
        if (itemsToDelete.isNotEmpty()) {
            repository.deleteItemsByIds(itemsToDelete.toList())
        }
        disableSelectionMode() // Exit selection mode after deleting
    }

    // Example function to insert data, called from a fragment
    fun insert(translation: TranslationHistory) = viewModelScope.launch {
        repository.insert(translation)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }
}
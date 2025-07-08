package com.asadbyte.translatorapp.presentation.bookmark

import android.app.Application
import androidx.lifecycle.*
import com.asadbyte.translatorapp.data.room.TranslationHistory
import com.asadbyte.translatorapp.presentation.main.TranslatorApplication
import kotlinx.coroutines.launch

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TranslatorApplication).repository

    val bookmarkedItems: LiveData<List<TranslationHistory>> = repository.bookmarkedItems.asLiveData()

    // This will be called when the user clicks the bookmark icon to remove it
    fun unbookmarkItem(item: TranslationHistory) = viewModelScope.launch {
        // Create a copy of the item with isBookmarked set to false
        val updatedItem = item.copy(isBookmarked = false)
        repository.update(updatedItem)
    }
}
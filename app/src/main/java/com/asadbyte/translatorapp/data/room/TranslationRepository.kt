package com.asadbyte.translatorapp.data.room

import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {

    // This Flow will automatically emit new values when the database changes
    val allHistory: Flow<List<TranslationHistory>> = translationDao.getAllHistory()
    val bookmarkedItems: Flow<List<TranslationHistory>> = translationDao.getBookmarkedItems()

    suspend fun insert(translation: TranslationHistory) {
        translationDao.insert(translation)
    }

    suspend fun update(translation: TranslationHistory) { // <-- ADD THIS
        translationDao.update(translation)
    }

    suspend fun clearHistory() {
        translationDao.clearAllHistory()
    }
}
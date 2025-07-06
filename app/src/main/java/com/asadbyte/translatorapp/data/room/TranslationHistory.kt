package com.asadbyte.translatorapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val isBookmarked: Boolean = false, // <-- ADD THIS FIELD
    val timestamp: Long = System.currentTimeMillis()
)
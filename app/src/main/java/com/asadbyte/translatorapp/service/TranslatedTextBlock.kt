package com.asadbyte.translatorapp.service

import android.graphics.Rect

// In its own file: TranslatedTextBlock.kt
data class TranslatedTextBlock(
    val translatedText: String,
    val boundingBox: Rect
)
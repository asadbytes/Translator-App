package com.asadbyte.translatorapp.service

import android.graphics.Rect

// In its own file: TranslatedTextBlock.kt
data class TranslatedTextBlock(
    val text: String,
    val bounds: Rect
)
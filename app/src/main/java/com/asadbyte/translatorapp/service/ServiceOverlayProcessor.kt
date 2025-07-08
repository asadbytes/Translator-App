package com.asadbyte.translatorapp.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface

class ServiceOverlayProcessor {

    fun createOverlay(screenshot: Bitmap, translatedBlocks: List<TranslatedTextBlock>): Bitmap {
        val overlayBitmap = screenshot.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(overlayBitmap)

        for (block in translatedBlocks) {
            val rect = block.boundingBox

            // Calculate appropriate text size based on bounding box
            val textSize = calculateTextSize(block.translatedText, rect)

            // Setup paints with calculated text size
            val backgroundPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = Color.argb(250, 255, 255, 255) // Almost opaque white
            }

            val textPaint = Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
            }

            val strokePaint = Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = Color.WHITE
                typeface = Typeface.DEFAULT_BOLD
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            // Create background box with some padding
            val padding = 8f
            val backgroundRect = RectF(
                rect.left.toFloat() - padding,
                rect.top.toFloat() - padding,
                rect.right.toFloat() + padding,
                rect.bottom.toFloat() + padding
            )

            // Draw background to hide original text
            canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)

            // Draw translated text
            val textBounds = Rect()
            textPaint.getTextBounds(block.translatedText, 0, block.translatedText.length, textBounds)

            val textX = rect.left.toFloat() + (rect.width() - textBounds.width()) / 2f
            val textY = rect.top.toFloat() + (rect.height() + textBounds.height()) / 2f

            // Draw text with stroke for better visibility
            canvas.drawText(block.translatedText, textX, textY, strokePaint)
            canvas.drawText(block.translatedText, textX, textY, textPaint)
        }

        return overlayBitmap
    }

    private fun calculateTextSize(text: String, rect: Rect): Float {
        val maxWidth = rect.width().toFloat() * 0.9f // Leave some margin
        val maxHeight = rect.height().toFloat() * 0.8f // Leave some margin

        val paint = Paint()
        var textSize = 12f
        val maxTextSize = 48f

        while (textSize < maxTextSize) {
            paint.textSize = textSize
            val textBounds = Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)

            if (textBounds.width() > maxWidth || textBounds.height() > maxHeight) {
                break
            }
            textSize += 2f
        }

        return maxOf(12f, textSize - 2f) // Ensure minimum readable size
    }

    private fun splitTextToFitWidth(text: String, paint: Paint, maxWidth: Int): List<String> {
        if (maxWidth <= 0) return listOf(text)

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)

            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    // Single word is too long, add it anyway
                    lines.add(word)
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines.ifEmpty { listOf(text) }
    }
}
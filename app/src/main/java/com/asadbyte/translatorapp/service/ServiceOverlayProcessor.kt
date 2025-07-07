package com.asadbyte.translatorapp.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

class ServiceOverlayProcessor {
    fun createOverlay(originalBitmap: Bitmap, translatedData: List<TranslatedTextBlock>): Bitmap {
        // Create a mutable copy of the original bitmap
        val overlayBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(overlayBitmap)

        // Paint for the background rectangle (semi-transparent black)
        val backgroundPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0) // Semi-transparent black
            style = Paint.Style.FILL
        }

        // Paint for the translated text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f // Increased text size for better visibility
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        // Paint for text stroke (outline)
        val strokePaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            typeface = Typeface.DEFAULT_BOLD
        }

        translatedData.forEach { block ->
            val bounds = block.bounds

            // Add some padding to the rectangle
            val padding = 8f
            val expandedBounds = RectF(
                bounds.left - padding,
                bounds.top - padding,
                bounds.right + padding,
                bounds.bottom + padding
            )

            // Draw rounded rectangle background
            canvas.drawRoundRect(expandedBounds, 8f, 8f, backgroundPaint)

            // Calculate text position
            val centerX = bounds.centerX().toFloat()
            val centerY = bounds.centerY().toFloat()

            // Handle multi-line text if needed
            val lines = splitTextToFitWidth(block.text, textPaint, bounds.width())
            val lineHeight = textPaint.textSize + 4f
            val totalHeight = lines.size * lineHeight
            val startY = centerY - totalHeight / 2 + lineHeight / 2

            lines.forEachIndexed { index, line ->
                val y = startY + index * lineHeight

                // Draw text stroke first (outline)
                canvas.drawText(line, centerX, y, strokePaint)

                // Draw the actual text
                canvas.drawText(line, centerX, y, textPaint)
            }
        }

        return overlayBitmap
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
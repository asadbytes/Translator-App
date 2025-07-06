package com.asadbyte.translatorapp.utils

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// A data class to hold the necessary info for drawing
data class TranslatedTextBlock(
    val translatedText: String,
    val boundingBox: Rect
)

class ImageOverlayProcessor {

    /**
     * Creates a new bitmap with translated text drawn over the original text locations.
     * This is a suspend function to ensure heavy image processing is done off the main thread.
     *
     * @param originalBitmap The base image.
     * @param translatedBlocks A list of objects containing translated text and their original bounding boxes.
     * @return A new Bitmap with the text overlay.
     */
    suspend fun createOverlayBitmap(
        originalBitmap: Bitmap,
        translatedBlocks: List<TranslatedTextBlock>
    ): Bitmap = withContext(Dispatchers.Default) {
        // Create a mutable copy of the original bitmap to draw on
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()

        for (block in translatedBlocks) {
            // Step 1: "Erase" the original text by drawing a white rectangle over it.
            // For a more advanced implementation, you could sample the color from the
            // edge of the bounding box.
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRect(block.boundingBox, paint)

            // Step 2: Draw the new, translated text
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT

            // Adjust text size dynamically to fit the bounding box width
            paint.textSize = getFittedTextSize(paint, block.translatedText, block.boundingBox.width().toFloat())

            // Calculate position to draw the text
            val x = block.boundingBox.left.toFloat()
            val y = block.boundingBox.top.toFloat() + paint.textSize // Position at the top-left

            canvas.drawText(block.translatedText, x, y, paint)
        }

        return@withContext mutableBitmap
    }

    /**
     * Calculates the optimal text size to fit the given text within a specific width.
     */
    private fun getFittedTextSize(paint: Paint, text: String, width: Float): Float {
        var textSize = 40f // Start with a reasonable default
        paint.textSize = textSize

        while (paint.measureText(text) > width && textSize > 8f) {
            textSize -= 1f
            paint.textSize = textSize
        }
        return textSize
    }
}
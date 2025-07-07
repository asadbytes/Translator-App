package com.asadbyte.translatorapp.service

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.ExecutionException

class ServiceTranslationRepository {

    /**
     * Translates a given text from a source language to a target language using ML Kit.
     * This function is synchronous within its coroutine scope and handles model downloads.
     *
     * @param text The text to be translated.
     * @param sourceLanguage The BCP-47 language code of the source text (e.g., "en").
     * @param targetLanguage The BCP-47 language code for the translation (e.g., "ur").
     * @return The translated text as a String, or null if translation fails.
     */
    fun translate(text: String, sourceLanguage: String, targetLanguage: String): String? {
        // 1. Create translator options with source and target languages
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        // 2. Get a translator instance with these options
        val translator = Translation.getClient(options)

        // 3. Download the required models if they aren't already on the device.
        // This is a blocking call to ensure models are ready before proceeding.
        val downloadConditions = DownloadConditions.Builder()
            .requireWifi() // You can change this to allow downloads over mobile data
            .build()

        try {
            // Tasks.await() makes this synchronous, which is fine inside a Dispatchers.IO coroutine.
            Tasks.await(translator.downloadModelIfNeeded(downloadConditions))
        } catch (e: Exception) {
            // This can happen if the download fails (e.g., no network)
            Log.e("TranslationRepository", "Failed to download language model.", e)
            translator.close()
            return null
        }

        // 4. Perform the translation
        return try {
            // Block and wait for the translation to complete.
            val translatedText = Tasks.await(translator.translate(text))
            Log.d("TranslationRepository", "Translation successful: '$text' -> '$translatedText'")
            translatedText
        } catch (e: ExecutionException) {
            // The translation task failed.
            Log.e("TranslationRepository", "ML Kit translation failed.", e.cause)
            null
        } catch (e: Exception) {
            // Any other unexpected exception.
            Log.e("TranslationRepository", "An unexpected error occurred during translation.", e)
            null
        } finally {
            // 5. ALWAYS close the translator to free up resources.
            translator.close()
        }
    }
}
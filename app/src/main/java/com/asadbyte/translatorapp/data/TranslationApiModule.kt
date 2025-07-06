package com.asadbyte.translatorapp.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

// A simple sealed class for results
sealed class TranslationResult {
    data class Success(val text: String) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
}

class TranslationApiModule {

    suspend fun translate(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ): TranslationResult {
        // 1. Download models if needed
        val downloadResult = downloadModels(sourceLangCode, targetLangCode)
        if (downloadResult is TranslationResult.Error) {
            return downloadResult
        }

        // 2. Perform translation
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        val translator = Translation.getClient(options)

        return try {
            val translatedText = translator.translate(text).await()
            translator.close()
            TranslationResult.Success(translatedText)
        } catch (e: Exception) {
            translator.close()
            TranslationResult.Error(e.message ?: "An unknown translation error occurred")
        }
    }

    private suspend fun downloadModels(sourceLangCode: String, targetLangCode: String): TranslationResult? {
        val modelManager = RemoteModelManager.getInstance()
        val sourceModel = TranslateRemoteModel.Builder(sourceLangCode).build()
        val targetModel = TranslateRemoteModel.Builder(targetLangCode).build()
        val conditions = DownloadConditions.Builder().build()

        return try {
            modelManager.download(sourceModel, conditions).await()
            modelManager.download(targetModel, conditions).await()
            null // Signifies success
        } catch (e: Exception) {
            TranslationResult.Error(e.message ?: "Failed to download language model")
        }
    }

    fun getLanguageCode(languageName: String): String? {
        return when (languageName) {
            "English" -> TranslateLanguage.ENGLISH
            "Spanish" -> TranslateLanguage.SPANISH
            "French" -> TranslateLanguage.FRENCH
            "German" -> TranslateLanguage.GERMAN
            "Italian" -> TranslateLanguage.ITALIAN
            "Portuguese" -> TranslateLanguage.PORTUGUESE
            "Russian" -> TranslateLanguage.RUSSIAN
            "Chinese" -> TranslateLanguage.CHINESE
            "Japanese" -> TranslateLanguage.JAPANESE
            "Korean" -> TranslateLanguage.KOREAN
            "Arabic" -> TranslateLanguage.ARABIC
            "Hindi" -> TranslateLanguage.HINDI
            "Urdu" -> TranslateLanguage.URDU
            else -> null // Language not supported
        }
    }

    fun getLocaleForSpeech(languageName: String): String {
        return when (languageName) {
            "English" -> "en-US"
            "Urdu" -> "ur-PK" // Urdu (Pakistan)
            "Spanish" -> "es-ES"
            "French" -> "fr-FR"
            "German" -> "de-DE"
            "Italian" -> "it-IT"
            "Portuguese" -> "pt-PT"
            "Russian" -> "ru-RU"
            "Chinese" -> "zh-CN"
            "Japanese" -> "ja-JP"
            "Korean" -> "ko-KR"
            "Arabic" -> "ar-SA" // Arabic (Saudi Arabia)
            "Hindi" -> "hi-IN"
            // Add other mappings as needed
            else -> "en-US" // Default to English if no match
        }
    }
}
package com.asadbyte.translatorapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object TextToSpeechManager : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        // This check prevents re-initialization if it's already done
        if (tts == null) {
            Log.d("TTS_Manager_Debug", "INITIALIZING TTS...")
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                Log.d("TTS_Manager_Debug", "onInit: SUCCESS! TTS Engine is ready.")
                isInitialized = true
            }
            else -> {
                Log.e("TTS_Manager_Debug", "onInit: FAILED! Status code: $status")
                isInitialized = false
            }
        }
    }

    fun speak(text: String, locale: Locale) {
        Log.d("TTS_Manager_Debug", "SPEAK() called. isInitialized = $isInitialized")
        if (!isInitialized) {
            Log.e("TTS_Manager_Debug", "Cannot speak, TTS not ready.")
            // Consider re-initializing as a fallback
            // tts?.let { Log.d("TTS_Manager_Debug", "TTS object exists, trying to re-init maybe?") }
            return
        }

        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS_Manager_Debug", "Language not supported or data missing: $locale")
        } else {
            Log.d("TTS_Manager_Debug", "Speaking text: '$text' in language: $locale")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    fun shutdown() {
        Log.w("TTS_Manager_Debug", "SHUTDOWN() called!")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
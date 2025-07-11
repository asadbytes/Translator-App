package com.asadbyte.translatorapp.presentation.main

import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.asadbyte.translatorapp.data.room.AppDatabase
import com.asadbyte.translatorapp.data.room.TranslationRepository
import com.asadbyte.translatorapp.utils.TextToSpeechManager

class TranslatorApplication : Application(), CameraXConfig.Provider {

    // Create the database instance using lazy delegation, so it's only created when needed
    val database by lazy { AppDatabase.getDatabase(this) }

    // Create the repository instance, passing the DAO from the database
    val repository by lazy { TranslationRepository(database.translationDao()) }

    companion object {
        const val CHANNEL_ID = "ScreenTranslatorServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        TextToSpeechManager.initialize(this)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen Translator Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
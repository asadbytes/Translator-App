package com.asadbyte.translatorapp.main

import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import android.app.Application
import com.asadbyte.translatorapp.data.room.AppDatabase
import com.asadbyte.translatorapp.data.room.TranslationRepository
import com.asadbyte.translatorapp.utils.TextToSpeechManager

class TranslatorApplication : Application(), CameraXConfig.Provider {

    // Create the database instance using lazy delegation, so it's only created when needed
    val database by lazy { AppDatabase.getDatabase(this) }

    // Create the repository instance, passing the DAO from the database
    val repository by lazy { TranslationRepository(database.translationDao()) }

    override fun onCreate() {
        super.onCreate()
        TextToSpeechManager.initialize(this)
    }
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}
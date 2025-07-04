package com.asadbyte.translatorapp.main

import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import android.app.Application

class TranslatorApplication : Application(), CameraXConfig.Provider {

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}
package com.asadbyte.translatorapp.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.main.TranslatorApplication

class ScreenTranslatorService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBubbleView: View

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        // A static variable that any part of the app can check
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the persistent notification
        val notification: Notification = NotificationCompat.Builder(this, TranslatorApplication.CHANNEL_ID)
            .setContentTitle("Screen Translator Active")
            .setContentText("Tap the floating icon to translate.")
            .setSmallIcon(R.drawable.ic_magnifier) // Use an appropriate icon
            .build()

        // Call startForeground to display the notification and keep the service running
        // The ID must be a non-zero integer.
        startForeground(1, notification)

        // If the service is killed, it will be automatically restarted
        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Log.d("ScreenTranslatorService", "Service created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the floating bubble layout
        floatingBubbleView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

        // Define layout parameters for the bubble
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // Add the view to the window
        windowManager.addView(floatingBubbleView, params)

        // Set up the click listener for the bubble
        val bubbleIcon = floatingBubbleView.findViewById<ImageView>(R.id.floating_bubble_icon)
        bubbleIcon.setOnClickListener {
            Log.d("ScreenTranslatorService", "service button clicked")
            // This is where you will start the screen capture process
            // For now, let's just show a Toast
            Toast.makeText(this, "Bubble Clicked!", Toast.LENGTH_SHORT).show()
            // We will add the MediaProjection logic here later
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ScreenTranslatorService", "Service destroyed")
        isRunning = true
        // Remove the bubble when the service is destroyed
        if (::floatingBubbleView.isInitialized) {
            windowManager.removeView(floatingBubbleView)
        }
    }
}
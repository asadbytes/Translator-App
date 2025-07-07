package com.asadbyte.translatorapp.service

import android.app.Activity
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.main.TranslatorApplication

class ScreenTranslatorService : Service() {

    companion object {
        var isRunning = false
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_PROJECTION_RESULT = "ACTION_PROJECTION_RESULT"

        fun start(context: Context) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // Helper to stop the service
        fun stop(context: Context) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        // Helper function to start the service with the projection result
        fun startServiceWithProjection(context: Context, resultCode: Int, data: Intent?) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_PROJECTION_RESULT
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            context.startService(intent)
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBubbleView: View
    private var overlayView: View? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    // You would get these from a repository or shared preferences
    private var sourceLanguage = "English"
    private var targetLanguage = "Urdu"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // This is where you call startForeground
                val notification: Notification = NotificationCompat.Builder(this, TranslatorApplication.CHANNEL_ID)
                    .setContentTitle("Screen Translator Active")
                    .setContentText("Tap the floating icon to translate.")
                    .setSmallIcon(R.drawable.ic_magnifier)
                    .build()
                startForeground(1, notification)
            }
            ACTION_PROJECTION_RESULT -> {
                // Handle the permission result from the activity
                val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>("data")

                if (resultCode == Activity.RESULT_OK && data != null) {
                    val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                    captureScreen()
                }
            }
            ACTION_STOP -> {
                // Stop the service
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Log.d("ScreenTranslatorService", "Service created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showFloatingBubble()
    }

    private fun showFloatingBubble() {
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

        floatingBubbleView.findViewById<ImageView>(R.id.floating_bubble_icon).setOnClickListener {
            // When bubble is clicked, hide it and start the permission request activity
            floatingBubbleView.visibility = View.GONE
            val intent = Intent(this, PermissionRequestActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun captureScreen() {
        // This is complex logic to get a screenshot
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )

        // Wait a moment for the display to populate
        Handler(Looper.getMainLooper()).postDelayed({
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()

                // Now you have the screenshot!
                processAndShowOverlay(bitmap)
            }
        }, 300)
    }

    private fun processAndShowOverlay(screenshot: Bitmap) {
        // HERE is where you would call your ImageTranslationProcessor
        // For now, let's just show the screenshot in an overlay
        showOverlay(screenshot)
    }

    private fun showOverlay(bitmap: Bitmap) {
        if (!isRunning) {
            return
        }
        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_translation_overlay, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            // REMOVED FLAG_NOT_TOUCHABLE and kept FLAG_NOT_FOCUSABLE
            // This allows your button to be clicked, but doesn't steal keyboard focus.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, params)

        val overlayImageView = overlayView!!.findViewById<ImageView>(R.id.overlay_image_view)
        val closeButton = overlayView!!.findViewById<ImageView>(R.id.close_button)

        overlayImageView.setImageBitmap(bitmap) // Display the processed bitmap here

        closeButton.setOnClickListener {
            hideOverlay()
        }
    }

    private fun hideOverlay() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        floatingBubbleView.visibility = View.VISIBLE // Show the bubble again

        // Clean up projection
        mediaProjection?.stop()
        mediaProjection = null
        virtualDisplay?.release()
        virtualDisplay = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ScreenTranslatorService", "Service destroyed")
        isRunning = false
        if (::floatingBubbleView.isInitialized) windowManager.removeView(floatingBubbleView)
        hideOverlay()
    }
}
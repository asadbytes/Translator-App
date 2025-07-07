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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenTranslatorService : Service() {

    private val translationRepository = ServiceTranslationRepository()
    private val overlayProcessor = ServiceOverlayProcessor()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        var isRunning = false
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_PROJECTION_RESULT = "ACTION_PROJECTION_RESULT"
        private const val ACTION_REQUEST_PROJECTION = "ACTION_REQUEST_PROJECTION"

        private var sourceLanguage = "en"
        private var targetLanguage = "ur"

        fun setLanguages(source: String, target: String) {
            sourceLanguage = source
            targetLanguage = target
        }

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

        fun stop(context: Context) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun startServiceWithProjection(context: Context, resultCode: Int, data: Intent?) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_PROJECTION_RESULT
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            context.startService(intent)
        }

        fun requestProjection(context: Context) {
            val intent = Intent(context, ScreenTranslatorService::class.java).apply {
                action = ACTION_REQUEST_PROJECTION
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

    private var hasPermission = false
    private var permissionResultCode: Int = Activity.RESULT_CANCELED
    private var permissionData: Intent? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification: Notification = NotificationCompat.Builder(this, TranslatorApplication.CHANNEL_ID)
                    .setContentTitle("Screen Translator Active")
                    .setContentText("Tap the floating icon to translate.")
                    .setSmallIcon(R.drawable.ic_magnifier)
                    .build()
                startForeground(1, notification)

                // Request screen capture permission immediately when service starts
                requestScreenCapturePermission()
            }
            ACTION_REQUEST_PROJECTION -> {
                requestScreenCapturePermission()
            }
            ACTION_PROJECTION_RESULT -> {
                val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>("data")

                if (resultCode == Activity.RESULT_OK && data != null) {
                    hasPermission = true
                    permissionResultCode = resultCode
                    permissionData = data

                    val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

                    Log.d("ScreenTranslatorService", "Screen capture permission granted")
                } else {
                    Log.d("ScreenTranslatorService", "Screen capture permission denied")
                    hasPermission = false
                }
            }
            ACTION_STOP -> {
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

    private fun requestScreenCapturePermission() {
        val intent = Intent(this, PermissionRequestActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showFloatingBubble() {
        floatingBubbleView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

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

        windowManager.addView(floatingBubbleView, params)

        floatingBubbleView.findViewById<ImageView>(R.id.floating_bubble_icon).setOnClickListener {
            onBubbleClicked()
        }
    }

    private fun onBubbleClicked() {
        if (!hasPermission || permissionData == null) {
            Log.d("ScreenTranslatorService", "No screen capture permission, requesting...")
            requestScreenCapturePermission()
            return
        }

        floatingBubbleView.visibility = View.GONE

        if (mediaProjection == null) {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(permissionResultCode, permissionData!!)
        }

        captureScreen()
    }

    private fun captureScreen() {
        // Clean up previous resources
        virtualDisplay?.release()
        imageReader?.close()

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

        Handler(Looper.getMainLooper()).postDelayed({
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                try {
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

                    processAndShowOverlay(bitmap)
                } catch (e: Exception) {
                    Log.e("ScreenTranslatorService", "Error processing screen capture", e)
                    showBubbleAgain()
                }
            } else {
                Log.e("ScreenTranslatorService", "Failed to acquire screen capture image.")
                showBubbleAgain()
            }
        }, 300)
    }

    private fun processAndShowOverlay(screenshot: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(screenshot, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isBlank()) {
                    Log.d("ScreenTranslatorService", "No text found on screen.")
                    showBubbleAgain()
                    return@addOnSuccessListener
                }

                serviceScope.launch(Dispatchers.IO) {
                    try {
                        val translatedBlocks = visionText.textBlocks.mapNotNull { block ->
                            translationRepository.translate(block.text, sourceLanguage, targetLanguage)?.let { translatedText ->
                                block.boundingBox?.let { bounds ->
                                    TranslatedTextBlock(translatedText, bounds)
                                }
                            }
                        }

                        if (translatedBlocks.isEmpty()) {
                            Log.d("ScreenTranslatorService", "Translation resulted in no valid text blocks.")
                            withContext(Dispatchers.Main) {
                                showBubbleAgain()
                            }
                        } else {
                            val overlayBitmap = overlayProcessor.createOverlay(screenshot, translatedBlocks)
                            withContext(Dispatchers.Main) {
                                showOverlay(overlayBitmap)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ScreenTranslatorService", "Error during translation", e)
                        withContext(Dispatchers.Main) {
                            showBubbleAgain()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScreenTranslatorService", "Text recognition failed", e)
                showBubbleAgain()
            }
    }

    private fun showOverlay(bitmap: Bitmap) {
        if (!isRunning) return

        // Remove any existing overlay
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e("ScreenTranslatorService", "Error removing old overlay", e)
            }
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_translation_overlay, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(overlayView, params)

            val overlayImageView = overlayView!!.findViewById<ImageView>(R.id.overlay_image_view)
            val closeButton = overlayView!!.findViewById<ImageView>(R.id.close_button)

            overlayImageView.setImageBitmap(bitmap)

            closeButton.setOnClickListener {
                hideOverlay()
            }
        } catch (e: Exception) {
            Log.e("ScreenTranslatorService", "Error showing overlay", e)
            showBubbleAgain()
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e("ScreenTranslatorService", "Error hiding overlay", e)
            }
        }
        overlayView = null
        showBubbleAgain()
    }

    private fun showBubbleAgain() {
        if (::floatingBubbleView.isInitialized) {
            floatingBubbleView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ScreenTranslatorService", "Service is being destroyed.")
        isRunning = false

        // Cancel all coroutines
        serviceScope.cancel()

        // Stop screen capture
        mediaProjection?.stop()
        mediaProjection = null

        // Clean up resources
        virtualDisplay?.release()
        imageReader?.close()

        // Clean up views
        if (::floatingBubbleView.isInitialized) {
            try {
                windowManager.removeView(floatingBubbleView)
            } catch (e: Exception) {
                Log.e("ScreenTranslatorService", "Error removing bubble on destroy", e)
            }
        }

        hideOverlay()
    }
}
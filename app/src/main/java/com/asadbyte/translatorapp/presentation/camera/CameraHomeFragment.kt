package com.asadbyte.translatorapp.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraHomeBinding
import com.asadbyte.translatorapp.presentation.main.HomeViewModel
import com.asadbyte.translatorapp.presentation.translation.LanguageKeys.KEY_SOURCE
import com.asadbyte.translatorapp.presentation.translation.LanguageKeys.KEY_TARGET
import com.asadbyte.translatorapp.presentation.translation.LanguageKeys.REQUEST_KEY
import java.io.File

class CameraHomeFragment : Fragment(R.layout.fragment_camera_home) {

    private lateinit var binding: FragmentCameraHomeBinding // Use ViewBinding
    private var imageCapture: ImageCapture? = null
    private val viewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)
    private val cameraViewModel: CameraViewModel by navGraphViewModels(R.id.nav_graph)

    // ActivityResultLauncher for camera permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Start Camera.
                startCamera()
            } else {
                // Explain to the user that the feature is unavailable
                Toast.makeText(
                    context,
                    "Camera permission is required to use this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // The callback receives the URI of the selected image
            uri?.let {
                // Check if the user actually selected an image
                // Use the same navigation action as your takePhoto() function
                val action = CameraHomeFragmentDirections.actionCameraHomeFragmentToCameraCropFragment(it.toString())
                findNavController().navigate(action)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCameraHomeBinding.bind(view)

        viewModel.sourceLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.sourceLangButton.text = languageName
        }
        viewModel.targetLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.targetLangButton.text = languageName
        }
        binding.swapIcon.setOnClickListener {
            viewModel.swapLanguages()
        }

        cameraViewModel.isFlashEnabled.observe(viewLifecycleOwner) { isEnabled ->
            if (isEnabled) {
                binding.bottomIconFlash.setImageResource(R.drawable.ic_camera_flash_enabled)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
            } else {
                binding.bottomIconFlash.setImageResource(R.drawable.ic_camera_flash_disabled)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
            }
        }

        binding.bottomIconFlash.setOnClickListener {
            cameraViewModel.toggleFlash()

            // Optional: Show a toast based on the new state
            val toastMessage = if (cameraViewModel.isFlashEnabled.value == true) "Flash ON" else "Flash OFF"
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }

        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            // We have a result!
            val selectedLanguage = bundle.getString("selectedLanguage")
            val requesterKey = bundle.getString("requesterKey")

            // Update the correct TextView based on who made the request
            if (selectedLanguage != null) {
                when (requesterKey) {
                    KEY_SOURCE -> viewModel.updateSourceLanguage(selectedLanguage)
                    KEY_TARGET -> viewModel.updateTargetLanguage(selectedLanguage)
                }
            }
        }

        binding.sourceLangButton.setOnClickListener {
            val action =
                CameraHomeFragmentDirections.actionCameraHomeFragmentToLanguageSelectionFragment(
                    requesterKey = KEY_SOURCE
                )
            findNavController().navigate(action)
        }

        binding.targetLangButton.setOnClickListener {
            val action =
                CameraHomeFragmentDirections.actionCameraHomeFragmentToLanguageSelectionFragment(
                    requesterKey = KEY_TARGET
                )
            findNavController().navigate(action)
        }

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomIconClick.setOnClickListener {
            takePhoto()
        }

        binding.bottomIconImages.setOnClickListener {
            // Launch the gallery, asking for any image type
            galleryLauncher.launch("image/*")
        }
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                startCamera()
            }

            else -> {
                // You can directly ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }

            // ImageCapture Use Case
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                // Bind use cases to camera, including the new imageCapture
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create a time-stamped file to hold the image
        val photoFile = File(
            requireContext().filesDir, // Use app's private storage
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)

                    // Navigate to the next fragment, passing the URI
                    val action =
                        CameraHomeFragmentDirections.actionCameraHomeFragmentToCameraCropFragment(
                            savedUri.toString()
                        )
                    findNavController().navigate(action)
                }
            }
        )
    }
}
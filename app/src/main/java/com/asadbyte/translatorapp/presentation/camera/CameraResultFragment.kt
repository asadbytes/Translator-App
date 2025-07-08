package com.asadbyte.translatorapp.presentation.camera

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraResultBinding
import com.asadbyte.translatorapp.presentation.main.HomeViewModel
import com.asadbyte.translatorapp.utils.TextToSpeechManager
import java.io.IOException
import java.util.Locale

class CameraResultFragment : Fragment() {
    private var _binding: FragmentCameraResultBinding? = null
    private val binding get() = _binding!!
    private val args: CameraResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeViewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)
        val cameraViewModel: CameraViewModel by navGraphViewModels(R.id.nav_graph)

        val sourceLanguage = homeViewModel.sourceLanguage.value ?: "English"
        val targetLanguage = homeViewModel.targetLanguage.value ?: "Urdu"

        val imageUri = Uri.parse(args.imageUri)

        // Set the image in your ImageView right away
        binding.capturedImageView.setImageURI(imageUri)

        // Call processImage ONLY ONCE
        cameraViewModel.processImage(imageUri, requireContext(), sourceLanguage, targetLanguage)

        // You only need one observer for the processing state
        cameraViewModel.processingState.observe(viewLifecycleOwner) { state ->
            if (state != "done") { // Avoid showing a "done" toast
                Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
            }
        }

        // The rest of your observers and listeners are fine...
        cameraViewModel.overlaidBitmap.observe(viewLifecycleOwner) { translatedBitmap ->
            if (binding.cameraResultSwitch.isChecked) {
                binding.capturedImageView.setImageBitmap(translatedBitmap)
            }
        }

        binding.bottomIconSpeaker.setOnClickListener {
            if(binding.cameraResultSwitch.isChecked){
                val currentTargetLang = homeViewModel.targetLanguage.value ?: "Urdu"
                val localeTag = homeViewModel.getLocaleForSpeech(currentTargetLang)
                val targetLocale = Locale.forLanguageTag(localeTag)
                TextToSpeechManager.speak(cameraViewModel.translatedText.value!!, targetLocale)
            } else {
                val currentSourceLang = homeViewModel.sourceLanguage.value ?: "English"
                val localeTag = homeViewModel.getLocaleForSpeech(currentSourceLang)
                val sourceLocale = Locale.forLanguageTag(localeTag)
                TextToSpeechManager.speak(cameraViewModel.recognizedText.value!!, sourceLocale)
            }
        }

        binding.cameraResultSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to "Translated" state
                // Use the bitmap from LiveData if it's available
                cameraViewModel.overlaidBitmap.value?.let {
                    binding.capturedImageView.setImageBitmap(it)
                }
            } else {
                // Switch to "Original" state
                // Use the original bitmap from the ViewModel
                cameraViewModel.originalBitmap.value?.let {
                    binding.capturedImageView.setImageBitmap(it)
                }
            }
        }

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomIconCamera.setOnClickListener {
            findNavController().popBackStack() // current
            findNavController().popBackStack() // one before
        }

        binding.bottomIconCopy.setOnClickListener {
            val original = cameraViewModel.recognizedText.value ?: ""
            val translated = cameraViewModel.translatedText.value ?: ""

            // Navigate with both text results
            val action = CameraResultFragmentDirections.actionCameraResultFragmentToCameraCopyFragment(original, translated)
            findNavController().navigate(action)
        }

        binding.bottomIconDownload.setOnClickListener {
            if(binding.cameraResultSwitch.isChecked)
                cameraViewModel.overlaidBitmap.value?.let {
                    downloadImage(it)
                }
            else
                cameraViewModel.originalBitmap.value?.let {
                    downloadImage(it)
                }
        }
    }

    // Place this function inside your CameraResultFragment class

    private fun downloadImage(bitmap: Bitmap) {
        // Create a unique filename for the image using the current time
        val fileName = "TranslatedImage_${System.currentTimeMillis()}.jpg"

        // Get the ContentResolver
        val resolver = requireActivity().contentResolver

        // Define the image details for MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            // For Android 10 (API 29) and above, you can specify a subdirectory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TranslatorApp")
            }
        }

        var imageUri: Uri? = null

        try {
            // Insert the new image record into the MediaStore
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (imageUri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }

            // Open an output stream to the URI and save the bitmap
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }

            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            // If there's an error, delete the incomplete image record
            imageUri?.let { resolver.delete(it, null, null) }

            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            Log.e("DownloadImage", "Error saving image", e)
        }
    }
}
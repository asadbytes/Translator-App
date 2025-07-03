package com.asadbyte.translatorapp.camera

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraCropBinding
import kotlinx.coroutines.launch
import java.io.File

class CameraCropFragment : Fragment(R.layout.fragment_camera_crop) {

    private lateinit var binding: FragmentCameraCropBinding
    private val args: CameraCropFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCameraCropBinding.bind(view)

        val imageUri = Uri.parse(args.imageUri)

        // Set the image in the CropImageView
        binding.capturedImageView.setImageUriAsync(imageUri)

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomTranslateButton.setOnClickListener {
            // Use a coroutine to handle the cropping in the background
            viewLifecycleOwner.lifecycleScope.launch {
                // Get the cropped result as a bitmap
                val bitmap = binding.capturedImageView.croppedImage

                // Save the cropped bitmap to a new file
                val croppedImageFile = File(requireContext().cacheDir, "cropped_image.jpg")
                croppedImageFile.outputStream().use { out ->
                    bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }

                // Get the Uri for the new file
                val croppedImageUri = croppedImageFile.toUri()

                // Navigate with the new Uri
                val action = CameraCropFragmentDirections.actionCameraCropFragmentToCameraResultFragment(croppedImageUri.toString())
                findNavController().navigate(action)
            }
        }
    }
}
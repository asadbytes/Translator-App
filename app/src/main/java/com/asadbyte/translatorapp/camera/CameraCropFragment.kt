package com.asadbyte.translatorapp.camera

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraCropBinding

class CameraCropFragment : Fragment(R.layout.fragment_camera_crop) {

    private lateinit var binding: FragmentCameraCropBinding
    private val args: CameraCropFragmentArgs by navArgs() // Delegate to get arguments

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCameraCropBinding.bind(view)

        // Get the URI string from arguments and parse it back to a Uri
        val imageUri = Uri.parse(args.imageUri)

        // Set the image in your ImageView
        binding.capturedImageView.setImageURI(imageUri)

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }
        // Add any other logic for back buttons, etc.
        binding.bottomTranslateButton.setOnClickListener {
            val action = CameraCropFragmentDirections.actionCameraCropFragmentToCameraResultFragment(imageUri.toString())
            findNavController().navigate(action)
        }
    }
}
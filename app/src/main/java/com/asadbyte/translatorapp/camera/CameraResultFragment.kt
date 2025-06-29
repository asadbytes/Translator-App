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
import com.asadbyte.translatorapp.databinding.FragmentCameraResultBinding
import com.asadbyte.translatorapp.databinding.FragmentHomeBinding

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

        val imageUri = Uri.parse(args.imageUri)

        // Set the image in your ImageView
        binding.capturedImageView.setImageURI(imageUri)

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomIconCopy.setOnClickListener {
            findNavController().navigate(R.id.action_cameraResultFragment_to_cameraCopyFragment)
        }
    }

}
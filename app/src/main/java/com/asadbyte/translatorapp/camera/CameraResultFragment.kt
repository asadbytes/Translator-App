package com.asadbyte.translatorapp.camera

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraResultBinding
import com.asadbyte.translatorapp.main.HomeViewModel

class CameraResultFragment : Fragment() {
    private var _binding: FragmentCameraResultBinding? = null
    private val binding get() = _binding!!
    private val args: CameraResultFragmentArgs by navArgs()
    private val cameraViewModel: CameraViewModel by viewModels()

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
        var sourceLanguage = homeViewModel.sourceLanguage.value
        var targetLanguage = homeViewModel.targetLanguage.value

        val imageUri = Uri.parse(args.imageUri)

        homeViewModel.sourceLanguage.observe(viewLifecycleOwner) { languageName ->
            sourceLanguage = languageName
        }
        homeViewModel.targetLanguage.observe(viewLifecycleOwner) { languageName ->
            targetLanguage = languageName
        }

        // Set the image in your ImageView
        binding.capturedImageView.setImageURI(imageUri)
        cameraViewModel.processImage(imageUri, requireContext(), sourceLanguage, targetLanguage)

        cameraViewModel.processingState.observe(viewLifecycleOwner) { state ->
            Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
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
    }
}
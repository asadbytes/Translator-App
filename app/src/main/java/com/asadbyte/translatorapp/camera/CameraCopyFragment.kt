package com.asadbyte.translatorapp.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraCopyBinding
import com.asadbyte.translatorapp.databinding.FragmentCameraResultBinding

class CameraCopyFragment : Fragment() {
    private var _binding: FragmentCameraCopyBinding? = null
    private val binding get() = _binding!!
    private val args: CameraCopyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraCopyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.originalTextView.text = args.originalText
        binding.translatedTextView.text = args.translatedText

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.translatedCopyIcon.setOnClickListener {
            copyToClipboard(binding.translatedTextView.text)
        }
    }

    private fun copyToClipboard(textToCopy: CharSequence) {
        // 1. Get the Clipboard Manager from the context
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 2. Create a ClipData object
        val clipData = ClipData.newPlainText("text", textToCopy)

        // 3. Set the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData)
    }
}
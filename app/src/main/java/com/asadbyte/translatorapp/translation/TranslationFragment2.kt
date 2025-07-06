package com.asadbyte.translatorapp.translation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.databinding.FragmentTranslation2Binding
import android.text.method.KeyListener
import android.widget.Toast
import androidx.navigation.fragment.navArgs

class TranslationFragment2 : Fragment() {

    private var _binding: FragmentTranslation2Binding? = null
    private val binding get() = _binding!!
    private var editable = false
    private var originalKeyListener: KeyListener? = null
    private val args: TranslationFragment2Args by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslation2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.originalTextView.setText(args.originalText)
        binding.translatedTextView.text = args.translatedText

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.pencilIcon.setOnClickListener {
            if (editable) {
                binding.originalTextView.inputType = InputType.TYPE_NULL
                editable = false
            } else {
                binding.originalTextView.inputType = InputType.TYPE_CLASS_TEXT
                editable = true
            }
        }

        // --- SETUP ---
        // 1. Save the original listener right after the view is created
        originalKeyListener = binding.originalTextView.keyListener

        // 2. Make it non-editable BY DEFAULT
        binding.originalTextView.keyListener = null

        // This code is correct and needs no changes.
        binding.originalCopyIcon.setOnClickListener {
            copyToClipboard(binding.originalTextView.text)
        }

        binding.translatedCopyIcon.setOnClickListener {
            copyToClipboard(binding.translatedTextView.text)
        }

        binding.pencilIcon.setOnClickListener {
            // Check if it's currently editable by seeing if the keyListener is not null
            val isEditable = binding.originalTextView.keyListener != null

            if (isEditable) {
                // If it IS editable, make it NOT editable
                binding.originalTextView.keyListener = null
            } else {
                // If it is NOT editable, make it editable by restoring the original listener
                binding.originalTextView.keyListener = originalKeyListener

                // Optional: Move cursor to the end and show the keyboard
                binding.originalTextView.requestFocus()
                binding.originalTextView.setSelection(binding.originalTextView.text.length)
                // You may need to add code to explicitly show the keyboard here
            }
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



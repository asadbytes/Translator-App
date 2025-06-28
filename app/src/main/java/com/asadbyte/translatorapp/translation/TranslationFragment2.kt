package com.asadbyte.translatorapp.translation

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.databinding.FragmentTranslation2Binding
import android.text.method.KeyListener

class TranslationFragment2 : Fragment() {

    private var _binding: FragmentTranslation2Binding? = null
    private val binding get() = _binding!!
    private var editable = false
    private var originalKeyListener: KeyListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslation2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.pencilIcon.setOnClickListener {
            if (editable) {
                binding.textInputArea.inputType = InputType.TYPE_NULL
                editable = false
            } else {
                binding.textInputArea.inputType = InputType.TYPE_CLASS_TEXT
                editable = true
            }
        }

        // --- SETUP ---
        // 1. Save the original listener right after the view is created
        originalKeyListener = binding.textInputArea.keyListener

        // 2. Make it non-editable BY DEFAULT
        binding.textInputArea.keyListener = null

        binding.pencilIcon.setOnClickListener {
            // Check if it's currently editable by seeing if the keyListener is not null
            val isEditable = binding.textInputArea.keyListener != null

            if (isEditable) {
                // If it IS editable, make it NOT editable
                binding.textInputArea.keyListener = null
            } else {
                // If it is NOT editable, make it editable by restoring the original listener
                binding.textInputArea.keyListener = originalKeyListener

                // Optional: Move cursor to the end and show the keyboard
                binding.textInputArea.requestFocus()
                binding.textInputArea.setSelection(binding.textInputArea.text.length)
                // You may need to add code to explicitly show the keyboard here
            }
        }
    }
}



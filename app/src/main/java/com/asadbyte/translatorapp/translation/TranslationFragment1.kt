package com.asadbyte.translatorapp.translation

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentTranslation1Binding

class TranslationFragment1 : Fragment() {

    private var _binding: FragmentTranslation1Binding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslation1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textArea = binding.textInputArea
        val crossButton = binding.crossButton
        val translateButton = binding.translateButton
        val micIcon = binding.micIcon

        crossButton.setOnClickListener {
            textArea.text.clear()
        }

        translateButton.setOnClickListener{
            findNavController().navigate(R.id.action_translationFragment1_to_translationFragment2)
        }

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        textArea.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // not needed for now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // not needed yet
            }

            override fun afterTextChanged(s: Editable?) {
                val hasText = s.toString().isNotEmpty()

                crossButton.visibility = if (hasText) View.VISIBLE else View.GONE
                translateButton.visibility = if (hasText) View.VISIBLE else View.GONE
                micIcon.visibility = if (hasText) View.GONE else View.VISIBLE
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
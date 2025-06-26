package com.asadbyte.translatorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.databinding.FragmentLanguageSelectionBinding

class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The fragment's own back button
        val backIcon = binding.backIcon

        backIcon.setOnClickListener {
            // Simply pop the back stack to go back to the previous fragment (Home)
            findNavController().popBackStack()
        }

        // ... setup your RecyclerView etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.asadbyte.translatorapp.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentLanguageSelectionBinding

class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: LanguageSelectionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.isForUser2) {
            // If it's for User 2, rotate the entire fragment's view by 180 degrees
            binding.root.rotation = 180f
        }

        // --- SETUP RECYCLERVIEW ---
        setupRecyclerView()
        // --------------------------

        // The fragment's own back button
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // 1. Create your dummy list of languages
        val languageList = listOf(
            Language("English"),
            Language("Español (Spanish)"),
            Language("Français (French)"),
            Language("Deutsch (German)"),
            Language("Italiano (Italian)"),
            Language("Português (Portuguese)"),
            Language("Русский (Russian)"),
            Language("中文 (Chinese)"),
            Language("日本語 (Japanese)"),
            Language("한국어 (Korean)"),
            Language("العربية (Arabic)"),
            Language("हिन्दी (Hindi)"),
            Language("اردو (Urdu)")
        )

        // 2. Create an instance of the adapter
        val languageAdapter = LanguageAdapter(languageList)

        // 3. Set the Layout Manager and Adapter for the RecyclerView
        binding.languageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
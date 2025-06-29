package com.asadbyte.translatorapp.language

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentLanguageSelectionBinding
import androidx.core.view.isVisible

class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: LanguageSelectionFragmentArgs by navArgs()

    private lateinit var languageAdapter: LanguageAdapter
    private val fullLanguageList = listOf(
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

        val inputText = binding.toolbarInput
        if (args.isForUser2) {
            // If it's for User 2, rotate the entire fragment's view by 180 degrees
            binding.root.rotation = 180f
        }

        // --- SETUP RECYCLERVIEW ---
        setupRecyclerView()
        // --------------------------

        setupSearch()

        // The fragment's own back button
        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchIcon.setOnClickListener {
            if(inputText.isVisible)
                inputText.visibility = View.GONE
            else
                inputText.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter(fullLanguageList) { isSelected, selectedLanguage ->
            updateToolbarForSelection(isSelected, selectedLanguage)
        }

        binding.languageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }
    }

    private fun updateToolbarForSelection(isSelected: Boolean, selectedLanguage: Language?) {
        if (isSelected) {
            binding.doneButton.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.GONE

            // You can now use the selected language's name!
            binding.toolbarInput.setText(selectedLanguage?.name)
            binding.toolbarInput.visibility = View.VISIBLE

        } else {
            binding.doneButton.visibility = View.GONE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.toolbarInput.visibility = View.GONE
        }
    }


    private fun setupSearch() {
        // Assuming your search EditText has the id "search_input"
        binding.toolbarInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filter(query)
            }
        })
    }

    private fun filter(query: String) {
        // Create a new list to hold the filtered items
        val filteredList = mutableListOf<Language>()

        // Loop through the original, full list
        for (language in fullLanguageList) {
            // Check if the language name contains the search query (case-insensitive)
            if (language.name.contains(query, ignoreCase = true)) {
                filteredList.add(language)
            }
        }

        // Update the adapter with the filtered list
        languageAdapter.filterList(filteredList)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
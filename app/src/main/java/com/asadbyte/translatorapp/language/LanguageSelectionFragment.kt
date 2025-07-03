package com.asadbyte.translatorapp.language

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentLanguageSelectionBinding
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult

class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: LanguageSelectionFragmentArgs by navArgs()

    private lateinit var languageAdapter: LanguageAdapter
    private var selectedLanguage: Language? = null
    private val fullLanguageList = listOf(
        // Using the new data class with unique IDs
        Language("English"), Language("Spanish"), Language("French"),
        Language("German"), Language("Italian"), Language("Portuguese"),
        Language("Russian"), Language("Chinese"), Language("Japanese"),
        Language("Korean"), Language("Arabic"), Language("Hindi"),
        Language("Urdu")
    )

    // Flag to prevent search from triggering when we set text programmatically
    private var isSelectionUpdate = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.isForUser2) {
            binding.root.rotation = 180f
        }

        setupRecyclerView()
        setupSearch()

        binding.backIcon.setOnClickListener { findNavController().popBackStack() }

        binding.searchIcon.setOnClickListener {
            binding.toolbarInput.visibility = if (binding.toolbarInput.isVisible) View.GONE else View.VISIBLE
            binding.toolbarTitle.visibility = if (binding.toolbarInput.isVisible) View.GONE else View.VISIBLE
        }

        binding.doneButton.setOnClickListener {
            Log.d("NAV_DEBUG", "Done button clicked!")
            // Check if a language is actually selected
            selectedLanguage?.let { lang ->
                Log.d("NAV_DEBUG", "Language is selected, preparing result.")
                // Create a bundle to pass the data back
                val result = bundleOf(
                    "selectedLanguage" to lang.name,
                    "requesterKey" to args.requesterKey // Pass the original requester key back
                )
                // Set the result with a request key that HomeFragment will listen for
                setFragmentResult("language_selection_request", result)
                // Go back to the previous screen
                findNavController().popBackStack()
            }
        }
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { isSelected, language ->
            // Update the currently selected language
            this.selectedLanguage = language
            updateToolbarForSelection(isSelected, language)
        }
        binding.languageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }
        languageAdapter.submitList(fullLanguageList)
    }

    private fun updateToolbarForSelection(isSelected: Boolean, selectedLanguage: Language?) {
        if (isSelected && selectedLanguage != null) {
            binding.doneButton.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.GONE
            binding.toolbarInput.visibility = View.VISIBLE

            isSelectionUpdate = true
            binding.toolbarInput.setText(selectedLanguage.name)
            binding.toolbarInput.setSelection(binding.toolbarInput.length())
            isSelectionUpdate = false

        } else {
            binding.doneButton.visibility = View.GONE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.toolbarInput.visibility = View.GONE

            // --- APPLY THE FIX HERE AS WELL ---
            isSelectionUpdate = true // Set the flag to prevent the TextWatcher from firing
            binding.toolbarInput.setText("") // This is the line that was crashing
            isSelectionUpdate = false // Unset the flag
        }
    }

    private fun setupSearch() {
        binding.toolbarInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Only filter if the change was made by the user, not our selection code
                if (!isSelectionUpdate) {
                    filter(s.toString())
                }
            }
        })
    }

    private fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullLanguageList
        } else {
            fullLanguageList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        // Using ListAdapter's submitList for efficient, animated updates
        // REMOVED the completion lambda that was causing the crash.
        languageAdapter.submitList(filteredList)

        // The selection is cleared automatically because filtering changes the list.
        // We just need to tell the adapter its 'selectedPosition' is now invalid.
        languageAdapter.clearSelection()
        this.selectedLanguage = null
        updateToolbarForSelection(false, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
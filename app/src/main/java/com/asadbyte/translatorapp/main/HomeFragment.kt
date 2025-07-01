package com.asadbyte.translatorapp.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentHomeBinding
import com.asadbyte.translatorapp.translation.LanguageKeys.KEY_SOURCE
import com.asadbyte.translatorapp.translation.LanguageKeys.KEY_TARGET
import com.asadbyte.translatorapp.translation.LanguageKeys.REQUEST_KEY

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sourceLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.sourceLangButton.text = languageName
        }
        viewModel.targetLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.targetLangButton.text = languageName
        }

        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            // We have a result!
            val selectedLanguage = bundle.getString("selectedLanguage")
            val requesterKey = bundle.getString("requesterKey")

            // Update the correct TextView based on who made the request
            if (selectedLanguage != null) {
                when (requesterKey) {
                    KEY_SOURCE -> viewModel.updateSourceLanguage(selectedLanguage)
                    KEY_TARGET -> viewModel.updateTargetLanguage(selectedLanguage)
                }
            }
        }

        // Find the button inside the included layout
        val srcLangButton = binding.sourceLangButton
        val targetLangButton = binding.targetLangButton
        val messageIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_message)
        val cameraIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_camera)
        val settingsIcon = binding.homeTopbar.topBarIcon

        val textArea = binding.textInputArea
        val crossButton = binding.crossButton
        val translateButton = binding.translateButton
        val micIcon = binding.micIcon

        val blueCard = binding.blueCard.root
        val bottomBar = binding.bottomNavigation

        crossButton.setOnClickListener {
            textArea.text.clear()
        }

        translateButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_translationFragment2)
        }

        binding.swapIcon.setOnClickListener {
            viewModel.swapLanguages()
        }

        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        messageIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_messageFragment)
        }

        targetLangButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToLanguageSelectionFragment(
                requesterKey = KEY_TARGET
            )
            findNavController().navigate(action)
        }

        srcLangButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToLanguageSelectionFragment(
                requesterKey = KEY_SOURCE
            )
            findNavController().navigate(action)
        }

        binding.homeTopbar.topBarTitle.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_translationFragment2)
        }

        cameraIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cameraHomeFragment)
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
                blueCard.visibility = if (hasText) View.GONE else View.VISIBLE
                bottomBar.visibility = if (hasText) View.GONE else View.VISIBLE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
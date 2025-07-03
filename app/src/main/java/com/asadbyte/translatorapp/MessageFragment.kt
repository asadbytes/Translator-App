package com.asadbyte.translatorapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.databinding.FragmentMessageBinding
import com.asadbyte.translatorapp.main.HomeFragmentDirections
import com.asadbyte.translatorapp.main.HomeViewModel
import com.asadbyte.translatorapp.translation.LanguageKeys.KEY_SOURCE
import com.asadbyte.translatorapp.translation.LanguageKeys.KEY_TARGET
import com.asadbyte.translatorapp.translation.LanguageKeys.REQUEST_KEY

class MessageFragment : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sourceLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.languageButtonUser1.text = languageName
        }
        viewModel.targetLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.languageButtonUser2.text = languageName
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

        val homeIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_home)
        val cameraIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_camera)

        binding.swapIcon.setOnClickListener {
            viewModel.swapLanguages()
        }

        cameraIcon.setOnClickListener {
            findNavController().navigate(R.id.action_messageFragment_to_cameraHomeFragment)
        }

        homeIcon.setOnClickListener {
            findNavController().navigate(R.id.action_messageFragment_to_HomeFragment)
        }

        // Button for User 1 (no rotation)
        binding.languageButtonUser1.setOnClickListener {
            // We pass 'false' for the argument, or don't pass it to use the default value
            val action = MessageFragmentDirections.actionMessageFragmentToLanguageSelectionFragment(isForUser2 = false, requesterKey = KEY_SOURCE)
            findNavController().navigate(action)
        }

        // Button for User 2 (will be rotated)
        binding.languageButtonUser2.setOnClickListener {
            // We pass 'true' for the argument to indicate this is for User 2
            val action = MessageFragmentDirections.actionMessageFragmentToLanguageSelectionFragment(isForUser2 = true, requesterKey = KEY_TARGET)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
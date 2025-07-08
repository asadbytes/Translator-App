package com.asadbyte.translatorapp.presentation.message

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.data.TranslationResult
import com.asadbyte.translatorapp.databinding.FragmentMessageBinding
import com.asadbyte.translatorapp.utils.TextToSpeechManager
import java.util.Locale

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    // 1. Switched to the dedicated MessageViewModel
    private val viewModel: MessageViewModel by viewModels()
    private var isListeningForUser2 = false // Tracks which user is speaking

    // --- ActivityResultLaunchers for Permissions and Speech-to-Text ---

    private val speechToTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                if (!spokenText.isNullOrEmpty()) {
                    // Update the UI immediately and then request translation from the ViewModel
                    if (isListeningForUser2) {
                        binding.textViewUser2.text = spokenText
                        viewModel.translateMessage(spokenText, reverseDirection = true)
                    } else {
                        binding.textViewUser1.text = spokenText
                        viewModel.translateMessage(spokenText, reverseDirection = false)
                    }
                }
            }
        }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) Toast.makeText(requireContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }

    // --- Standard Fragment Lifecycle Methods ---

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        askAudioPermission()
        setupClickListeners()
        setupObservers()
        setupLanguageSelectionListener()
    }

    // --- Setup Functions ---

    private fun setupObservers() {
        // Observe language changes to update the buttons
        viewModel.sourceLanguage.observe(viewLifecycleOwner) {
            binding.languageButtonUser1.text = it
            if (it == "Urdu")
                binding.textViewUser1.typeface = ResourcesCompat.getFont(requireContext(), R.font.noto_nastaliq_urdu)
            else
                binding.textViewUser1.typeface = Typeface.DEFAULT

        }
        viewModel.targetLanguage.observe(viewLifecycleOwner) {
            binding.languageButtonUser2.text = it
            if (it == "Urdu")
                binding.textViewUser2.typeface = ResourcesCompat.getFont(requireContext(), R.font.noto_nastaliq_urdu)
            else
                binding.textViewUser2.typeface = Typeface.DEFAULT
        }

        // 2. Observe the final translation result
        viewModel.translationResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { (result, isForUser2) ->
                when (result) {
                    is TranslationResult.Success -> {
                        // Update the correct user's TextView with the translated text
                        if (isForUser2) {
                            binding.textViewUser2.text = result.text
                        } else {
                            binding.textViewUser1.text = result.text
                        }
                    }
                    is TranslationResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Observe the loading state to give user feedback
        viewModel.translationState.observe(viewLifecycleOwner) { state ->
            if (state.startsWith("Translating")) {
                Toast.makeText(context, "Translating...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // 3. Hook up the mic buttons
        binding.micIconUser1.setOnClickListener { startSpeechToText(isForUser2 = false) }
        binding.micIconUser2.setOnClickListener { startSpeechToText(isForUser2 = true) }

        binding.swapIcon.setOnClickListener { viewModel.swapLanguages() }

        // Navigation listeners
        val homeIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_home)
        val cameraIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_camera)
        homeIcon.setOnClickListener { findNavController().navigate(R.id.action_messageFragment_to_HomeFragment) }
        cameraIcon.setOnClickListener { findNavController().navigate(R.id.action_messageFragment_to_cameraHomeFragment) }
        binding.languageButtonUser1.setOnClickListener { navigateToLanguageSelection(isForUser2 = false) }
        binding.languageButtonUser2.setOnClickListener { navigateToLanguageSelection(isForUser2 = true) }

        binding.speakerIconUser1.setOnClickListener {
            val currentSourceLang = viewModel.sourceLanguage.value ?: "English"
            val localeTag = viewModel.getLocaleForSpeech(currentSourceLang)
            val sourceLocale = Locale.forLanguageTag(localeTag)
            TextToSpeechManager.speak(binding.textViewUser1.text.toString(), sourceLocale)
        }

        binding.speakerIconUser2.setOnClickListener {
            val currentTargetLang = viewModel.targetLanguage.value ?: "Urdu"
            val localeTag = viewModel.getLocaleForSpeech(currentTargetLang)
            val targetLocale = Locale.forLanguageTag(localeTag)
            TextToSpeechManager.speak(binding.textViewUser2.text.toString(), targetLocale)
        }
    }

    private fun setupLanguageSelectionListener() {
        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val lang = bundle.getString("selectedLanguage")
            val key = bundle.getString("requesterKey")
            if (lang != null) {
                when (key) {
                    KEY_SOURCE -> viewModel.updateSourceLanguage(lang)
                    KEY_TARGET -> viewModel.updateTargetLanguage(lang)
                }
            }
        }
    }

    // --- Helper Functions ---

    private fun navigateToLanguageSelection(isForUser2: Boolean) {
        val requesterKey = if (isForUser2) KEY_TARGET else KEY_SOURCE
        val action = MessageFragmentDirections.actionMessageFragmentToLanguageSelectionFragment(isForUser2, requesterKey)
        findNavController().navigate(action)
    }

    private fun startSpeechToText(isForUser2: Boolean) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            askAudioPermission()
            return
        }

        this.isListeningForUser2 = isForUser2
        val langName = if (isForUser2) viewModel.targetLanguage.value!! else viewModel.sourceLanguage.value!!
        val langCode = viewModel.getLocaleForSpeech(langName)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        try {
            speechToTextResultLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Speech-to-Text not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Define the keys here so the Fragment is self-contained
    companion object {
        const val REQUEST_KEY = "language_selection_request"
        const val KEY_SOURCE = "source"
        const val KEY_TARGET = "target"
    }
}
package com.asadbyte.translatorapp.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now show notifications.
                Toast.makeText(requireContext(), "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied. Explain to the user that notifications are disabled.
                Toast.makeText(requireContext(), "Notifications permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        askNotificationPermission()
        viewModel.sourceLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.sourceLangButton.text = languageName
        }
        viewModel.targetLanguage.observe(viewLifecycleOwner) { languageName ->
            binding.targetLangButton.text = languageName
        }


        viewModel.translatedText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { translatedText ->
                Toast.makeText(context, "Translation successful!", Toast.LENGTH_SHORT).show()
                val originalText = binding.textInputArea.text.toString()

                val action = HomeFragmentDirections.actionHomeFragmentToTranslationFragment2(
                    originalText = originalText,
                    translatedText = translatedText
                )
                findNavController().navigate(action)
            }
        }

        viewModel.translationError.observe(viewLifecycleOwner) { errorMessage ->
            // Failure! Show an error message to the user.
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.translationState.observe(viewLifecycleOwner) { state ->
            when {
                state.startsWith("Downloading") || state.startsWith("Translating") -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.statusText.visibility = View.VISIBLE
                    binding.statusText.text = state
                    binding.translateButton.isEnabled = false // Disable button
                }
                else -> { // This handles "done", "error", or null
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.visibility = View.GONE
                    binding.translateButton.isEnabled = true // Re-enable button
                }
            }
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

        translateButton.setOnClickListener {
            val textToTranslate = binding.textInputArea.text.toString()
            if (textToTranslate.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    Toast.makeText(context, "Translating...", Toast.LENGTH_SHORT).show()
                    viewModel.translate(textToTranslate)
                } else {
                    Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun askNotificationPermission() {
        // This is only necessary for API level 33 and above (Android 13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
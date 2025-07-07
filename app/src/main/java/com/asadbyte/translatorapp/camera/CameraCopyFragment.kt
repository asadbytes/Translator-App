package com.asadbyte.translatorapp.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentCameraCopyBinding
import com.asadbyte.translatorapp.databinding.FragmentCameraResultBinding
import com.asadbyte.translatorapp.main.HomeViewModel
import com.asadbyte.translatorapp.utils.TextToSpeechManager
import java.util.Locale

class CameraCopyFragment : Fragment() {
    private var _binding: FragmentCameraCopyBinding? = null
    private val binding get() = _binding!!
    private val args: CameraCopyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraCopyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)
        val cameraViewModel: CameraViewModel by navGraphViewModels(R.id.nav_graph)

        if(homeViewModel.sourceLanguage.value == "Urdu")
            binding.originalTextView.typeface = resources.getFont(R.font.noto_nastaliq_urdu)

        if(homeViewModel.targetLanguage.value == "Urdu")
            binding.translatedTextView.typeface = resources.getFont(R.font.noto_nastaliq_urdu)

        binding.originalTextSpeakerIcon.setOnClickListener {
            val currentSourceLang = homeViewModel.sourceLanguage.value ?: "English"
            val localeTag = homeViewModel.getLocaleForSpeech(currentSourceLang)
            val sourceLocale = Locale.forLanguageTag(localeTag)
            TextToSpeechManager.speak(binding.originalTextView.text.toString(), sourceLocale)
        }
        binding.translatedTextSpeakerIcon.setOnClickListener {
            val currentTargetLang = homeViewModel.targetLanguage.value ?: "Urdu"
            val localeTag = homeViewModel.getLocaleForSpeech(currentTargetLang)
            val targetLocale = Locale.forLanguageTag(localeTag)
            TextToSpeechManager.speak(binding.translatedTextView.text.toString(), targetLocale)
        }

        binding.originalTextView.text = args.originalText
        binding.translatedTextView.text = args.translatedText

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.translatedCopyIcon.setOnClickListener {
            copyToClipboard(binding.translatedTextView.text)
        }

        // Observe the current translation to update the bookmark icon's state
        cameraViewModel.currentTranslation.observe(viewLifecycleOwner) { translation ->
            if (translation?.isBookmarked == true) {
                binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_selected)
            } else {
                binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
            }
        }

        // Set the click listener for the bookmark icon
        binding.bookmarkIcon.setOnClickListener {
            cameraViewModel.toggleBookmark()
        }
    }

    private fun copyToClipboard(textToCopy: CharSequence) {
        // 1. Get the Clipboard Manager from the context
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 2. Create a ClipData object
        val clipData = ClipData.newPlainText("text", textToCopy)

        // 3. Set the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData)
    }
}
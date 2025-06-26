package com.asadbyte.translatorapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.databinding.FragmentMessageBinding

// Other imports remain the same

class MessageFragment : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_home)

        homeIcon.setOnClickListener {
            findNavController().navigate(R.id.action_messageFragment_to_homeFragment)
        }

        // Button for User 1 (no rotation)
        binding.languageButtonUser1.setOnClickListener {
            // We pass 'false' for the argument, or don't pass it to use the default value
            val action = MessageFragmentDirections.actionMessageFragmentToLanguageSelectionFragment(isForUser2 = false)
            findNavController().navigate(action)
        }

        // Button for User 2 (will be rotated)
        binding.languageButtonUser2.setOnClickListener {
            // We pass 'true' for the argument to indicate this is for User 2
            val action = MessageFragmentDirections.actionMessageFragmentToLanguageSelectionFragment(isForUser2 = true)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
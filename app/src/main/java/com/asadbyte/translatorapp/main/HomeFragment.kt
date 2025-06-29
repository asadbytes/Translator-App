package com.asadbyte.translatorapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button inside the included layout
        val leftButton = binding.leftButton
        val rightButton = binding.rightButton
        val messageIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_message)
        val cameraIcon = binding.bottomNavigation.findViewById<View>(R.id.bottom_nav_camera)
        val settingsIcon = binding.homeTopbar.topBarIcon

        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        messageIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_messageFragment)
        }

        rightButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_languageSelectionFragment)
        }

        // Set the click listener to navigate
        leftButton.setOnClickListener {
            // This is how you navigate using the action defined in the graph
            findNavController().navigate(R.id.action_homeFragment_to_languageSelectionFragment)
        }

        binding.homeTopbar.topBarTitle.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_translationFragment1)
        }

        cameraIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cameraHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
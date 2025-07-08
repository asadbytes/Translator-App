package com.asadbyte.translatorapp.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backIcon
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        val settingsData = createSettingsList()

        val settingsAdapter = SettingsAdapter(settingsData,
            onItemClick = { setting ->
                // --- THIS IS THE LOGIC YOU NEED TO ADD ---
                when (setting.id) {
                    // Navigation Actions
                    "language" -> Toast.makeText(context, "Clicked on ${setting.heading}", Toast.LENGTH_SHORT).show()
                    "bookmark" -> findNavController().navigate(R.id.action_settingsFragment_to_bookmarkFragment)
                    "history" -> findNavController().navigate(R.id.action_settingsFragment_to_historyFragment)

                    // Intent-based Actions (don't navigate within the app)
                    "rate" -> Toast.makeText(context, "Clicked on ${setting.heading}", Toast.LENGTH_SHORT).show()
                    "share" -> Toast.makeText(context, "Clicked on ${setting.heading}", Toast.LENGTH_SHORT).show()

                    // Clicks that might open a dialog or do nothing yet
                    "support", "about" -> {
                        Toast.makeText(context, "Clicked on ${setting.heading}", Toast.LENGTH_SHORT).show()
                    }

                    // The switch items probably don't need a row click action,
                    // as their action is the switch itself. But you could add one if needed.
                    "theme", "offline" -> {
                        // You can toggle the switch programmatically on row click if you want
                        // Or just let the switch handle the click itself.
                    }
                }
            },
            onSwitchCheckedChange = { setting, isChecked ->
                // Handle switch state change
                Toast.makeText(context, "${setting.heading} is now ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
                // Here you would save the state, e.g., to SharedPreferences
            }
        )

        binding.settingRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
    }

    private fun createSettingsList(): List<SettingItem> {
        // Use placeholder drawable names. Replace with your actual drawable resource names.
        return listOf(
            // Switch Items
            SettingItem("theme", R.drawable.ic_theme_setting, "App Theme", "Tap to change", SettingsAdapter.VIEW_TYPE_SWITCH),
            SettingItem("offline", R.drawable.ic_aeroplane, "Offline Translation", "Tap to change", SettingsAdapter.VIEW_TYPE_SWITCH),

            // Simple Items
            SettingItem("language", R.drawable.ic_globe, "Change Language", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("bookmark", R.drawable.ic_bookmark, "Bookmark", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("history", R.drawable.ic_history, "History", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("rate", R.drawable.ic_rate_us, "Rate Us", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("share", R.drawable.ic_share, "Share App", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("support", R.drawable.ic_cutomer_support, "Customer Support", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE),
            SettingItem("about", R.drawable.ic_about_app, "About App", "Tap to change", SettingsAdapter.VIEW_TYPE_SIMPLE)
        )
    }

    // ... other fragment lifecycle methods
}
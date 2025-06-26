package com.asadbyte.translatorapp.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup back button click to navigate up the stack
        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        // --- Setup RecyclerView ---

        // 1. Create sample data. In a real app, you would fetch this from a database.
        val sampleHistorys = listOf(
            History(1, "ہیلو آپ کیسے ہیں؟", "Hello how are you?", "Urdu"),
            History(2, "آپ کا نام کیا ہے؟", "What is your name?", "Urdu"),
            History(3, "میں ٹھیک ہوں شکریہ", "I am fine, thank you", "Urdu"),
            History(4, "یہ کتنے کا ہے؟", "How much is this?", "Urdu"),
            History(5, "الوداع", "Goodbye", "Urdu")
        )

        // 2. Create an instance of your adapter
        val bookmarksAdapter = HistoryAdapter(sampleHistorys,
            onItemClicked = { bookmark ->
                // This code runs when a user taps on any part of the card
                Toast.makeText(requireContext(), "Tapped on: ${bookmark.translatedText}", Toast.LENGTH_SHORT).show()
            },
            onIconClicked = { bookmark ->
                // This code runs when a user taps on the bookmark icon
                // You could, for example, show a dialog to confirm removing the bookmark.
                Toast.makeText(requireContext(), "Icon clicked for: ${bookmark.translatedText}", Toast.LENGTH_SHORT).show()
            }
        )

        // 3. Configure the RecyclerView
        binding.historyRecyclerView.apply {
            // A LayoutManager is required for a RecyclerView to function
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarksAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
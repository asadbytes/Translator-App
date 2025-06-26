package com.asadbyte.translatorapp.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentBookmarkBinding

class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
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
        val sampleBookmarks = listOf(
            Bookmark(1, "ہیلو آپ کیسے ہیں؟", "Hello how are you?"),
            Bookmark(2, "آپ کا نام کیا ہے؟", "What is your name?"),
            Bookmark(3, "میں ٹھیک ہوں شکریہ", "I am fine, thank you"),
            Bookmark(4, "یہ کتنے کا ہے؟", "How much is this?"),
            Bookmark(5, "الوداع", "Goodbye")
        )

        // 2. Create an instance of your adapter
        val bookmarksAdapter = BookmarksAdapter(sampleBookmarks,
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
        binding.languageRecyclerView.apply {
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
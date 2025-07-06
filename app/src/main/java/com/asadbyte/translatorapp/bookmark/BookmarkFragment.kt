package com.asadbyte.translatorapp.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.databinding.FragmentBookmarkBinding
import androidx.fragment.app.viewModels

class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookmarkViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the adapter and pass the ViewModel's function as the click listener
        val bookmarksAdapter = BookmarksAdapter { bookmark ->
            // When the icon is clicked, tell the ViewModel to unbookmark the item
            viewModel.unbookmarkItem(bookmark)
        }

        // Configure the RecyclerView
        binding.bookmarkRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarksAdapter
        }

        // Observe the list of bookmarked items from the ViewModel
        viewModel.bookmarkedItems.observe(viewLifecycleOwner) { bookmarks ->
            // The adapter will automatically handle updates
            bookmarksAdapter.submitList(bookmarks)
        }

        // Setup back button click
        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
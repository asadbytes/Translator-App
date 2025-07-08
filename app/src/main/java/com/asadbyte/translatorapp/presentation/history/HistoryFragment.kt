package com.asadbyte.translatorapp.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asadbyte.translatorapp.R
import com.asadbyte.translatorapp.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Create an instance of your new adapter. The constructor is now empty.
        val historyAdapter = HistoryAdapter(viewModel)

        // 2. Configure the RecyclerView
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        // 3. Observe the LiveData from the ViewModel
        viewModel.allHistory.observe(viewLifecycleOwner) { historyList ->
            // The ListAdapter's submitList method will handle updating the RecyclerView
            // automatically and efficiently.
            historyAdapter.submitList(historyList)
        }

        // Observe selection mode to show/hide UI elements
        viewModel.isSelectionModeActive.observe(viewLifecycleOwner) { isActive ->
            if (isActive) {
                binding.kebabMenuIcon.visibility = View.GONE
                binding.deleteIcon.visibility = View.VISIBLE
            } else {
                binding.kebabMenuIcon.visibility = View.VISIBLE
                binding.deleteIcon.visibility = View.GONE
            }
        }

        // Observe the set of selected items to redraw the list
        viewModel.selectedItems.observe(viewLifecycleOwner) { selectedIds ->
            historyAdapter.notifyDataSetChanged() // Redraw to show selection highlights
            // Update the title to show count, e.g., "3 selected"
            if (viewModel.isSelectionModeActive.value == true) {
                binding.toolbarTitle.text = "${selectedIds.size} selected"
            } else {
                binding.toolbarTitle.text = getString(R.string.history)
            }
        }

        binding.deleteIcon.setOnClickListener {
            viewModel.deleteSelectedItems()
        }

        binding.backIcon.setOnClickListener {
            // If in selection mode, disable it. Otherwise, navigate back.
            if (viewModel.isSelectionModeActive.value == true) {
                viewModel.disableSelectionMode()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.kebabMenuIcon.setOnClickListener {
            showHistoryMenu(it) // 'it' refers to the kebabMenuIcon view
        }
    }

    private fun showHistoryMenu(anchorView: View) {
        // 1. Create a PopupMenu instance
        val popup = PopupMenu(requireContext(), anchorView)

        // 2. Inflate the menu resource
        popup.menuInflater.inflate(R.menu.history_menu, popup.menu)

        // 3. Set a listener for menu item clicks
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_select -> {
                    viewModel.enableSelectionMode()
                    true
                }
                R.id.action_select_all -> {
                    viewModel.enableSelectionMode()
                    viewModel.selectAll()
                    true
                }
                else -> false
            }
        }

        // 4. Show the menu
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
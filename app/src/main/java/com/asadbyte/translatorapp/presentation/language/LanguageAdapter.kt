package com.asadbyte.translatorapp.presentation.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R

// Add this data class definition if you don't have it.
// The 'id' is important for DiffUtil.
data class Language(val name: String, val id: String = name)

class LanguageAdapter(
    private val onSelectionChanged: (isSelected: Boolean, selectedLanguage: Language?) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    private var selectedPosition = -1

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val languageName: TextView = itemView.findViewById(R.id.item_lang_name)
        val defaultIcon: ImageView = itemView.findViewById(R.id.item_lang_button_default)
        val selectedIcon: ImageView = itemView.findViewById(R.id.item_lang_button_selected)
        val container: RelativeLayout = itemView.findViewById(R.id.item_lang_container)

        init {
            itemView.setOnClickListener {
                val clickedPosition = adapterPosition
                if (clickedPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val previouslySelectedPosition = selectedPosition

                if (clickedPosition == previouslySelectedPosition) {
                    // --- CASE 1: DESELECTING THE CURRENT ITEM ---
                    selectedPosition = -1
                    onSelectionChanged(false, null)
                    // Now, we correctly notify the item that was just deselected.
                    notifyItemChanged(clickedPosition)

                } else {
                    // --- CASE 2: SELECTING A NEW ITEM ---
                    selectedPosition = clickedPosition
                    onSelectionChanged(true, getItem(clickedPosition))
                    // Notify the new item to show as selected
                    notifyItemChanged(clickedPosition)
                    // Notify the old item (if there was one) to show as deselected
                    if (previouslySelectedPosition != -1) {
                        notifyItemChanged(previouslySelectedPosition)
                    }
                }
            }
        }

        fun bind(language: Language) {
            languageName.text = language.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = getItem(position)
        holder.bind(language)

        if (position == selectedPosition) {
            // SELECTED STATE
            holder.container.setBackgroundResource(R.drawable.rounded_background_selected)
            holder.defaultIcon.visibility = View.GONE
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            // DEFAULT STATE
            holder.container.setBackgroundResource(R.drawable.rounded_background)
            holder.defaultIcon.visibility = View.VISIBLE
            holder.selectedIcon.visibility = View.GONE
        }
    }

    // This allows you to reset the selection from the fragment if needed
    fun clearSelection() {
        val previouslySelected = selectedPosition
        selectedPosition = -1
        if (previouslySelected != -1) {
            notifyItemChanged(previouslySelected)
        }
    }
}

// DiffUtil provides efficient list updates
class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
    override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem == newItem
    }
}
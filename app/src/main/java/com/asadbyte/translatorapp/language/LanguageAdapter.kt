package com.asadbyte.translatorapp.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R

// 1. Data class to hold the language information
data class Language(val name: String)

class LanguageAdapter(
    initialLanguages: List<Language>,
    private val onSelectionChanged: (isSelected: Boolean, selectedLanguage: Language?) -> Unit // Pass selected language back
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var languages: List<Language> = initialLanguages
    private var selectedPosition = -1

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val languageName: TextView = itemView.findViewById(R.id.item_lang_name)
        val defaultIcon: RadioButton = itemView.findViewById(R.id.item_lang_button_default)
        val selectedIcon: ImageView = itemView.findViewById(R.id.item_lang_button_selected)
        val container: RelativeLayout = itemView.findViewById(R.id.item_lang_container)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {

                    // --- THIS IS THE NEW, CORRECT LOGIC ---

                    // 1. Get the position of the previously selected item
                    val previousSelectedPosition = selectedPosition

                    // 2. Determine the new selected position
                    if (adapterPosition == selectedPosition) {
                        // If the user clicks the same item, deselect it
                        selectedPosition = -1
                        onSelectionChanged(false, null)
                    } else {
                        // Otherwise, select the new item
                        selectedPosition = adapterPosition
                        onSelectionChanged(true, languages[selectedPosition])
                    }

                    // 3. Notify only the items that have changed
                    if (previousSelectedPosition != -1) {
                        // Refresh the previously selected item to clear its state
                        notifyItemChanged(previousSelectedPosition)
                    }
                    // Refresh the newly selected item (or deselected item)
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.languageName.text = language.name

        if (selectedPosition == position) {
            // --- SELECTED STATE ---
            holder.container.setBackgroundResource(R.drawable.rounded_background_selected)
            holder.defaultIcon.visibility = View.GONE
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            // --- DEFAULT STATE ---
            holder.container.setBackgroundResource(R.drawable.rounded_background)
            holder.defaultIcon.visibility = View.VISIBLE
            holder.selectedIcon.visibility = View.GONE
        }
    }

    fun filterList(filteredList: List<Language>) {
        languages = filteredList
        selectedPosition = -1
        onSelectionChanged(false, null)
        // Use notifyDataSetChanged() here ONLY because the entire list content has changed
        notifyDataSetChanged()
    }
}
package com.asadbyte.translatorapp.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R

// 1. Data class to hold the language information
data class Language(val name: String)

// 2. The RecyclerView Adapter
class LanguageAdapter(private val languages: List<Language>) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    // This keeps track of the selected radio button's position
    private var selectedPosition = -1

    // 3. The ViewHolder class
    // This class holds the view references for a single list item.
    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val languageName: TextView = itemView.findViewById(R.id.language_name)
        val radioButton: RadioButton = itemView.findViewById(R.id.language_radio_button)

        init {
            // Set a click listener for the entire list item view
            itemView.setOnClickListener {
                // When an item is clicked, update the selected position
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    selectedPosition = adapterPosition
                    // Notify the adapter that the data has changed,
                    // which will re-render the list and update the radio buttons.
                    notifyDataSetChanged()
                }
            }
        }
    }

    // This method is called when a new ViewHolder is needed.
    // It inflates the layout for the list item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_language, parent, false)
        return LanguageViewHolder(view)
    }

    // This method returns the total number of items in the list.
    override fun getItemCount(): Int {
        return languages.size
    }

    // This method is called to display the data at a specific position.
    // It binds the data to the views in the ViewHolder.
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.languageName.text = language.name

        // Set the radio button's state based on whether its position
        // matches the currently selected position.
        holder.radioButton.isChecked = (selectedPosition == position)
    }
}

package com.asadbyte.translatorapp.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R

class HistoryAdapter(
    private val historyList: List<History>,
    private val onItemClicked: (History) -> Unit,
    private val onIconClicked: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    /**
     * The ViewHolder holds references to the views in your list_item_history.xml layout.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val inputText: TextView = itemView.findViewById(R.id.input_text)
        private val translatedText: TextView = itemView.findViewById(R.id.translated_text)
        private val languageText: TextView = itemView.findViewById(R.id.languageText)

        fun bind(history: History) {
            // Set the text for each history
            inputText.text = history.inputText
            translatedText.text = history.translatedText
            languageText.text = history.languageText


            // Set a click listener for the entire card
            itemView.setOnClickListener {
                onItemClicked(history)
            }

            // Set a click listener specifically for the history icon
            languageText.setOnClickListener {
                onIconClicked(history)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Inflate your list_item_history.xml layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        // Get the data for the current position and bind it to the ViewHolder
        val history = historyList[position]
        holder.bind(history)
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return historyList.size
    }
}

data class History(
    val id: Int, // A unique ID is good practice
    val inputText: String,
    val translatedText: String,
    val languageText: String
)
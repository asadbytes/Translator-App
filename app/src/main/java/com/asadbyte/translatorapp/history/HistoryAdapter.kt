package com.asadbyte.translatorapp.history

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.asadbyte.translatorapp.data.room.TranslationHistory

class HistoryAdapter : ListAdapter<TranslationHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    /**
     * The ViewHolder holds references to the views in your list_item_history.xml layout.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val originalText: TextView = itemView.findViewById(R.id.input_text)
        private val translatedText: TextView = itemView.findViewById(R.id.translated_text)
        private val languageText: TextView = itemView.findViewById(R.id.languageText)

        fun bind(history: TranslationHistory) {
            // Bind the data from your Room entity to the views
            originalText.text = history.originalText
            translatedText.text = history.translatedText
            // Example: Display a language pair like "Urdu -> English"
            languageText.text = "${history.sourceLanguage} -> ${history.targetLanguage}"

            if (history.sourceLanguage == "Urdu") {
                originalText.gravity = Gravity.START
                originalText.typeface = ResourcesCompat.getFont(itemView.context, R.font.noto_nastaliq_urdu)
            } else
                originalText.typeface = ResourcesCompat.getFont(itemView.context, R.font.poppins_semibold)

            // You can do the same for the translated text
            if (history.targetLanguage == "Urdu") {
                translatedText.gravity = Gravity.START
                translatedText.typeface = ResourcesCompat.getFont(itemView.context, R.font.noto_nastaliq_urdu)
            } else
                translatedText.typeface = ResourcesCompat.getFont(itemView.context, R.font.poppins_semibold)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        // ListAdapter provides getItem() to get the data for the current position
        val history = getItem(position)
        holder.bind(history)
    }
}

/**
 * DiffUtil helps ListAdapter determine which items have changed, been added, or been removed.
 */
class HistoryDiffCallback : DiffUtil.ItemCallback<TranslationHistory>() {
    override fun areItemsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
        return oldItem == newItem
    }
}
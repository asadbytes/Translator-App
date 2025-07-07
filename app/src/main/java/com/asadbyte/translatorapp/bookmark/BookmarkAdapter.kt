package com.asadbyte.translatorapp.bookmark

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.asadbyte.translatorapp.data.room.TranslationHistory

class BookmarksAdapter(
    private val onIconClicked: (TranslationHistory) -> Unit
) : ListAdapter<TranslationHistory, BookmarksAdapter.BookmarkViewHolder>(BookmarkDiffCallback()) {

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val originalText: TextView = itemView.findViewById(R.id.input_text)
        private val translatedText: TextView = itemView.findViewById(R.id.translated_text)
        private val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_icon)

        fun bind(bookmark: TranslationHistory) {
            originalText.text = bookmark.originalText
            translatedText.text = bookmark.translatedText

            if (bookmark.sourceLanguage == "Urdu") {
                originalText.gravity = Gravity.START
                originalText.typeface = ResourcesCompat.getFont(itemView.context, R.font.noto_nastaliq_urdu)
            } else
                originalText.typeface = ResourcesCompat.getFont(itemView.context, R.font.poppins_semibold)

            if (bookmark.targetLanguage == "Urdu") {
                translatedText.gravity = Gravity.START
                translatedText.typeface = ResourcesCompat.getFont(itemView.context, R.font.noto_nastaliq_urdu)
            } else
                translatedText.typeface = ResourcesCompat.getFont(itemView.context, R.font.poppins_semibold)

            bookmarkIcon.setOnClickListener {
                onIconClicked(bookmark)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BookmarkDiffCallback : DiffUtil.ItemCallback<TranslationHistory>() {
    override fun areItemsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
        return oldItem == newItem
    }
}
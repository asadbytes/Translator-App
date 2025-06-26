package com.asadbyte.translatorapp.bookmark

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.R

class BookmarksAdapter(
    private val bookmarks: List<Bookmark>,
    private val onItemClicked: (Bookmark) -> Unit,
    private val onIconClicked: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    /**
     * The ViewHolder holds references to the views in your list_item_bookmark.xml layout.
     */
    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val inputText: TextView = itemView.findViewById(R.id.input_text)
        private val translatedText: TextView = itemView.findViewById(R.id.translated_text)
        private val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_icon)

        fun bind(bookmark: Bookmark) {
            // Set the text for each bookmark
            inputText.text = bookmark.inputText
            translatedText.text = bookmark.translatedText

            // Set a click listener for the entire card
            itemView.setOnClickListener {
                onItemClicked(bookmark)
            }

            // Set a click listener specifically for the bookmark icon
            bookmarkIcon.setOnClickListener {
                onIconClicked(bookmark)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        // Inflate your list_item_bookmark.xml layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        // Get the data for the current position and bind it to the ViewHolder
        val bookmark = bookmarks[position]
        holder.bind(bookmark)
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return bookmarks.size
    }
}

// Bookmark.kt
data class Bookmark(
    val id: Int, // A unique ID is good practice
    val inputText: String,
    val translatedText: String
)
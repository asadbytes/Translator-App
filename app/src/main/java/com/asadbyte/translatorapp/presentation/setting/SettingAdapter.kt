package com.asadbyte.translatorapp.presentation.setting

// SettingsAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asadbyte.translatorapp.databinding.ListItemSettingSimpleBinding
import com.asadbyte.translatorapp.databinding.ListItemSettingSwitchBinding

class SettingsAdapter(
    private val settingsList: List<SettingItem>,
    private val onItemClick: (SettingItem) -> Unit,
    private val onSwitchCheckedChange: (SettingItem, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Define integer constants for each view type.
    companion object {
        const val VIEW_TYPE_SWITCH = 1
        const val VIEW_TYPE_SIMPLE = 2
    }

    // --- ViewHolder for the layout with a Switch ---
    inner class SwitchViewHolder(val binding: ListItemSettingSwitchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.icon1.setImageResource(item.iconResId)
            binding.heading.text = item.heading
            binding.subheading.text = item.subheading

            // Clear previous listeners to prevent unwanted triggers
            binding.settingSwitch.setOnCheckedChangeListener(null)

            // Here you would set the initial checked state from a source like SharedPreferences
            // For now, we'll set a placeholder
            binding.settingSwitch.isChecked = item.id == "theme" // Example: theme is on by default

            // Set the listener for switch changes
            binding.settingSwitch.setOnCheckedChangeListener { _, isChecked ->
                onSwitchCheckedChange(item, isChecked)
            }

            // Handle clicks on the entire row
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // --- ViewHolder for the layout with a simple icon ---
    inner class SimpleViewHolder(val binding: ListItemSettingSimpleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.icon1.setImageResource(item.iconResId)
            binding.heading.text = item.heading
            binding.subheading.text = item.subheading
            // The forward icon is already set in XML, but you could change it here if needed
            // binding.icon2.setImageResource(R.drawable.ic_forward)

            // Handle clicks on the entire row
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // This method tells the adapter which layout to use for a specific item.
    override fun getItemViewType(position: Int): Int {
        return settingsList[position].viewType
    }

    // This method creates the appropriate ViewHolder based on the viewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SWITCH -> {
                val binding = ListItemSettingSwitchBinding.inflate(inflater, parent, false)
                SwitchViewHolder(binding)
            }
            VIEW_TYPE_SIMPLE -> {
                val binding = ListItemSettingSimpleBinding.inflate(inflater, parent, false)
                SimpleViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // This method binds the data from your list to the views in the ViewHolder.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = settingsList[position]
        when (holder) {
            is SwitchViewHolder -> holder.bind(currentItem)
            is SimpleViewHolder -> holder.bind(currentItem)
        }
    }

    // This method returns the total number of items in the list.
    override fun getItemCount() = settingsList.size
}

// SettingItem.kt
data class SettingItem(
    val id: String, // A unique ID like "theme", "language", etc.
    val iconResId: Int,
    val heading: String,
    val subheading: String,
    val viewType: Int
)
package com.clipboardime.ui

import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.clipboardime.R
import com.clipboardime.data.ClipboardEntity
import com.clipboardime.databinding.ItemClipboardResultBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClipboardAdapter(
    private val onCopyClick: (ClipboardEntity) -> Unit
) : ListAdapter<ClipboardEntity, ClipboardAdapter.ViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    class ViewHolder(val binding: ItemClipboardResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClipboardResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvContent.text = item.content
        holder.binding.tvTimestamp.text = dateFormat.format(Date(item.timestamp))
        holder.binding.btnCopy.setOnClickListener { onCopyClick(item) }
        holder.itemView.setOnClickListener { onCopyClick(item) }
    }

    object DiffCallback : DiffUtil.ItemCallback<ClipboardEntity>() {
        override fun areItemsTheSame(old: ClipboardEntity, new: ClipboardEntity) =
            old.id == new.id

        override fun areContentsTheSame(old: ClipboardEntity, new: ClipboardEntity) =
            old.content == new.content && old.timestamp == new.timestamp
    }
}

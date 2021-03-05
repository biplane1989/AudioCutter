package com.example.audiocutter.functions.audiochooser.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.databinding.ItemFolderCutterBinding
import com.example.audiocutter.functions.audiochooser.objects.FolderItem

class FolderViewHolder(val binding: ItemFolderCutterBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FolderItem, listener: (FolderItem) -> Unit) {
        binding.tvFolder.text = item.folder
        binding.count.text = item.count.toString()

        binding.root.setOnClickListener {
            listener(item)
        }
    }

    companion object {
        fun create(parent: ViewGroup): FolderViewHolder {
            return FolderViewHolder(ItemFolderCutterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }
}
package com.example.audiocutter.functions.audiochooser.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.audiocutter.functions.audiochooser.objects.FolderItem


class FolderCutChooserAdapter(val listener: (FolderItem) -> Unit): ListAdapter<FolderItem, FolderViewHolder>(FloderCutItemDiffUnit()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

class FloderCutItemDiffUnit: DiffUtil.ItemCallback<FolderItem>() {
    override fun areItemsTheSame(oldItem: FolderItem, newItem: FolderItem) = oldItem.folder == newItem.folder

    override fun areContentsTheSame(oldItem: FolderItem, newItem: FolderItem) = oldItem == newItem
}
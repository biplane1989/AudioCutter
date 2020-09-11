package com.example.audiocutter.functions.mystudio

import androidx.recyclerview.widget.DiffUtil

class MusicDiffCallBack : DiffUtil.ItemCallback<AudioFileView>() {

    override fun areItemsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView == newItemView
    }

    override fun areContentsTheSame(
        oldItemView: AudioFileView,
        newItemView: AudioFileView
    ): Boolean {
        return oldItemView == newItemView
    }
}
package com.example.audiocutter.functions.mystudio

import androidx.recyclerview.widget.DiffUtil
import com.example.audiocutter.functions.mystudio.audiocutter.ItemLoadStatus

class MusicDiffCallBack : DiffUtil.ItemCallback<AudioFileView>() {

    override fun areItemsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView.audioFile.file.absoluteFile == newItemView.audioFile.file.absoluteFile
    }

    override fun areContentsTheSame(
        oldItemView: AudioFileView,
        newItemView: AudioFileView
    ): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: AudioFileView, newItem: AudioFileView): Any? {

        return newItem.itemLoadStatus
    }
}
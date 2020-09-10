package com.example.audiocutter.functions.audiocutterscreen


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.objects.AudioFile

class AudiocutterAdapter(val mContext: Context) :
    ListAdapter<AudioFile, AudiocutterAdapter.AudiocutterHolder>(Audiodiff()) {

    open lateinit var mCallBack: AudioCutterListtener

    fun setAudioCutterListtener(event: AudioCutterListtener) {
        mCallBack = event
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiocutterHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false)
        return AudiocutterHolder(view)
    }

    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int) {
        val itemAudioFile = getItem(position)
        holder.tvBitrateAudio.text = "${itemAudioFile.bitRate}"
        holder.tvNameAudio.text = itemAudioFile.fileName
        holder.tvSizeAudio.text = itemAudioFile.size.toString()
        holder.tvSizeAudio.tag = itemAudioFile


    }


    inner class AudiocutterHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio)

        init {
            ivController.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val itemAudio = tvSizeAudio.tag as AudioFile
            when (p0.id) {
                R.id.iv_controller_audio -> mCallBack.controllerMusic(itemAudio, ivController)
            }
        }
    }


    interface AudioCutterListtener {
        fun controllerMusic(audioFile: AudioFile, ivController: ImageView)
    }
}

class Audiodiff : DiffUtil.ItemCallback<AudioFile>() {
    override fun areItemsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
        return oldItem.uri == oldItem.uri
    }

    override fun areContentsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
        return oldItem.fileName == newItem.fileName
    }

}

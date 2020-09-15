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
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

class AudiocutterAdapter(val mContext: Context) :
    ListAdapter<AudioCutterView, AudiocutterAdapter.AudiocutterHolder>(Audiodiff()) {
    lateinit var mCallBack: AudioCutterListener

    var listAudios = mutableListOf<AudioCutterView>()
    var playerInfo: PlayerInfo? = null
    var currentHolder: AudiocutterHolder? = null

    fun setAudioCutterListtener(event: AudioCutterListener) {
        mCallBack = event
    }

    override fun submitList(list: List<AudioCutterView>?) {
        if (list == null) {
            listAudios.clear()
        } else {
            listAudios.clear()
            listAudios.addAll(list)
        }
        super.submitList(list)
        currentHolder = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiocutterHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false)
        return AudiocutterHolder(view)
    }

    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int) {
        val itemAudioFile = getItem(position)
        holder.bind()
        if (itemAudioFile.state == PlayerState.IDLE) {
            holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
        }
        if (itemAudioFile.state == PlayerState.PLAYING) {
            holder.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
        } else {
            holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
        }

        playerInfo?.let {
            if (it.playerState != PlayerState.IDLE && it.currentAudio == itemAudioFile.audioFile) {
                currentHolder = holder
            }
        }


    }


    inner class AudiocutterHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val SIZE_MB = 1024
        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio)

        fun bind() {
            val itemAudioFile = getItem(adapterPosition)

            tvBitrateAudio.text = "${itemAudioFile.audioFile.bitRate}Kbs/s"
            tvNameAudio.text = itemAudioFile.audioFile.fileName
            tvSizeAudio.text = (itemAudioFile.audioFile.size / SIZE_MB).toString() + "Mb"

            ivController.setOnClickListener(this)

        }

        override fun onClick(p0: View) {
            when (p0.id) {
                R.id.iv_controller_audio -> {
                    controllerAudio()
                }
            }
        }

        private fun controllerAudio() {
            currentHolder = this
            val itemAudioCutter = listAudios.get(adapterPosition)


                when (itemAudioCutter.state) {
                    PlayerState.IDLE -> {
                        itemAudioCutter.isPlaying = true
                        mCallBack.play(itemAudioCutter.audioFile)
                    }
                    PlayerState.PAUSE -> {
                        mCallBack.resume()
                    }
                    PlayerState.PLAYING -> {
                        mCallBack.pause()
                    }
            }

        }


    }

//    fun mediaInfoUpdate(playerInfo: PlayerInfo) {
//        val runningPos = getRunningAudioPos(playerInfo)
//
//        if (runningPos != -1) {
//            val audioFileView = listAudios.get(runningPos)
//            audioFileView.state = playerInfo.playerState
//            if (currentHolder != null) {
//                if (audioFileView.state == PlayerState.PLAYING) {
//                    currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
//                } else {
//                    currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_play)
//                }
//            }
//        }
//
//    }


    fun mediaInfoUpdate(playerInfo: PlayerInfo) {

        if (playerInfo.currentAudio == null) {
            // case 1
        } else {
            var selectedPosition = -1
            var i = 0
            while (i < listAudios.size) {
                if (listAudios.get(i).audioFile.file.absoluteFile.equals(playerInfo.currentAudio!!.file.absoluteFile)) {
                    selectedPosition = i
                    break
                }
                i++
            }
            if (selectedPosition == -1) {
                // case 1
            } else {
                // case 2
                val audioFileView = listAudios.get(selectedPosition)

                updateItemView(audioFileView, playerInfo)
//                updateSongInfo(playerInfo)
            }
        }
    }

    private fun updateItemView(audioCutterView: AudioCutterView, playerInfo: PlayerInfo) {
        if (currentHolder != null) {
            if (audioCutterView.state != playerInfo.playerState) {
                audioCutterView.state = playerInfo.playerState
                when (audioCutterView.state) {
                    PlayerState.PLAYING -> {
                        currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                    }
                    PlayerState.PAUSE -> {
                        currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    }
                    PlayerState.IDLE -> {
                        currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    }
                }
            }

//            updateTimeSong(playerInfo)
        }
    }


    private fun getRunningAudioPos(playerInfo: PlayerInfo): Int {
        var runningPos = -1
        var index = 0
        while (index < listAudios.size) {
            if (listAudios.get(index).audioFile == playerInfo.currentAudio) {
                runningPos = index
                break
            }
            index++
        }
        return runningPos
    }


    interface AudioCutterListener {

        fun play(audioFile: AudioFile)
        fun pause()
        fun resume()
        fun stop()
    }
}

class Audiodiff : DiffUtil.ItemCallback<AudioCutterView>() {
    override fun areItemsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == oldItem
    }

    override fun areContentsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == newItem
    }

}

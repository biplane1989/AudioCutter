package com.example.audiocutter.functions.mystudio.audiocutter

import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.MusicDiffCallBack
import com.example.audiocutter.objects.AudioFile
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

interface AudioCutterScreenCallback {
    fun play(audioFile: AudioFile)
    fun pause()
    fun resume()
}


class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) :
    ListAdapter<AudioFileView, AudioCutterAdapter.ViewHolder>(
        AsyncDifferConfig.Builder(MusicDiffCallBack())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor()).build()
    ) {
    private var listAudios = ArrayList<AudioFileView>()
    private var selectedViewHolder: ViewHolder? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioCutterAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.output_audio_manager_screen_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioCutterAdapter.ViewHolder, position: Int) {
        holder.bind()
        val audioFileView = listAudios.get(position)
        if (audioFileView.isExpanded) {
            holder.llPlayMusic.visibility = View.VISIBLE
        } else {
            holder.llPlayMusic.visibility = View.GONE
        }
        if (audioFileView.playerState == PlayerState.PLAYING) {
            holder.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_pause)
        } else {
            holder.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
        }
//        holder.bind(audioCutterClicked)
    }

    private fun updateItemView(audioFileView: AudioFileView, playerInfo: PlayerInfo) {
        if (selectedViewHolder != null) {
            if (audioFileView.playerState != playerInfo.playerState) {
                audioFileView.playerState = playerInfo.playerState
                when (audioFileView.playerState) {
                    PlayerState.PLAYING -> {
                        selectedViewHolder!!.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_pause)
                    }
                    PlayerState.PAUSE -> {
                        selectedViewHolder!!.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                    }
                    PlayerState.IDLE -> {
                        selectedViewHolder!!.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                    }
                }
            }
            simpleDateFormat = SimpleDateFormat("mm : ss")
            selectedViewHolder!!.tvTotal.text = simpleDateFormat.format(playerInfo.duration)
            selectedViewHolder!!.sbMusic.max = playerInfo.duration
            updateTimeSong(playerInfo)
        }
    }

    var simpleDateFormat = SimpleDateFormat()
    fun updateTimeSong(playerInfo: PlayerInfo) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {

                selectedViewHolder!!.tvTimeLife.text =
                    simpleDateFormat.format(playerInfo.position)
                selectedViewHolder!!.sbMusic.progress = playerInfo.position
                handler.postDelayed(this, 500)
//                if (sb_music.progress == sb_music.max) {
//                    Log.d("001", "onCompletion: sssssss")
//                }
//                mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener {
//                    onNextSong()
//                })
            }

        }, 100)


    }

    override fun submitList(list: MutableList<AudioFileView>?) {

        if (list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios)
        } else {
            listAudios = ArrayList()
            super.submitList(listAudios)
        }
    }

    fun updateMedia(playerInfo: PlayerInfo) {

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
            }
        }
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar_music)
        val ivSetting: ImageView = itemView.findViewById(R.id.iv_setting)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)

        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
        val clItem: ConstraintLayout = itemView.findViewById(R.id.cl_item)
        val ivPausePlay: ImageView = itemView.findViewById(R.id.iv_pause_play_music)
        val sbMusic: SeekBar = itemView.findViewById(R.id.sb_music)
        val tvTimeLife: TextView = itemView.findViewById(R.id.tv_time_life)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_time_total)

        fun bind() {
            val audioFileView = getItem(adapterPosition)

            tvTitle.setText(audioFileView.audioFile.fileName)
            tvInfo.setText(audioFileView.audioFile.size.toString() + " MB" + " | " + audioFileView.audioFile.bitRate.toString() + "kb/s")
            tvTotal.setText("/" + audioFileView.audioFile.time.toString())
            Glide.with(itemView.context).load(audioFileView.audioFile.file.absoluteFile)
                .transition(DrawableTransitionOptions.withCrossFade()).dontAnimate().into(ivAvatar)

            if (audioFileView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
            } else {
                llPlayMusic.visibility = View.GONE
            }

            clItem.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.cl_item -> {
                    val audioFileView = listAudios.get(adapterPosition)
                    audioFileView.isExpanded = !audioFileView.isExpanded;
                    notifyItemChanged(adapterPosition)
                }
                R.id.iv_pause_play_music -> {
                    selectedViewHolder = this@ViewHolder
                    val audioFileView = listAudios.get(adapterPosition)
                    when (audioFileView.playerState) {
                        PlayerState.IDLE -> {
                            audioCutterScreenCallback.play(audioFileView.audioFile)
                        }
                        PlayerState.PAUSE -> {
                            audioCutterScreenCallback.resume()
                        }
                        PlayerState.PLAYING -> {
                            audioCutterScreenCallback.pause()
                        }
                    }
                }
            }
        }


    }
}
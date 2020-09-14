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
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.functions.mystudio.MusicDiffCallBack
import com.example.audiocutter.objects.AudioFile
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

interface AudioCutterScreenCallback {
    fun play(audioFile: AudioFile)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Int)
}


class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) :
    ListAdapter<AudioFileView, AudioCutterAdapter.ViewHolder>(
        AsyncDifferConfig.Builder(MusicDiffCallBack())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor()).build()
    ) {
    private var listAudios = ArrayList<AudioFileView>()
    private var selectedViewHolder: ViewHolder? = null
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

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

            updateTimeSong(playerInfo)
        }
    }

    fun updateTimeSong(playerInfo: PlayerInfo) {
        selectedViewHolder!!.tvTotal.text = "/" + simpleDateFormat.format(playerInfo.duration)
        selectedViewHolder!!.sbMusic.max = playerInfo.duration

        selectedViewHolder!!.tvTimeLife.text =
            simpleDateFormat.format(playerInfo.position)
        selectedViewHolder!!.sbMusic.progress = playerInfo.position
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
//                updateSongInfo(playerInfo)
            }
        }
    }

    fun updateDeleteStatus(deleteState: DeleteState) {
        when (deleteState) {
            DeleteState.HIDE -> {
                listAudios.forEach {
                    it.deleteState = DeleteState.HIDE
                }
            }
            DeleteState.UNCHECK -> {
                listAudios.forEach {
                    it.deleteState = DeleteState.UNCHECK
                }
            }
            DeleteState.CHECKED -> {
                listAudios.forEach {
                    it.deleteState = DeleteState.CHECKED
                }
            }
        }
        notifyDataSetChanged()
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
        val ivItemDelete: ImageView = itemView.findViewById(R.id.iv_item_delete)

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

            when (audioFileView.deleteState) {

                DeleteState.HIDE -> {

                    ivItemDelete.visibility = View.GONE
                    ivSetting.visibility = View.VISIBLE
                }
                DeleteState.UNCHECK -> {
                    ivSetting.visibility = View.GONE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
                }
                DeleteState.CHECKED -> {
                    ivSetting.visibility = View.GONE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
                }
            }

            clItem.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            ivSetting.setOnClickListener(this)

            ivItemDelete.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    audioCutterScreenCallback.seekTo(sbMusic.progress)
                }

            })
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.cl_item -> {
                    val audioFileView = listAudios.get(adapterPosition)
                    when (audioFileView.deleteState) {

                        DeleteState.HIDE -> {
                            for (item in listAudios) {
                                if (item != listAudios.get(adapterPosition))
                                    item.isExpanded = false
                            }
                            audioFileView.isExpanded = !audioFileView.isExpanded
                            audioCutterScreenCallback.stop()

                            notifyDataSetChanged()
                        }
                        DeleteState.UNCHECK -> {
//                            val newAudioFileView = audioFileView.copy()
//                            newAudioFileView.deleteState = DeleteState.CHECKED
//                            listAudios.set(adapterPosition, newAudioFileView)
                            listAudios.get(adapterPosition).deleteState = DeleteState.CHECKED
                            ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)

                        }
                        DeleteState.CHECKED -> {
                            listAudios.get(adapterPosition).deleteState = DeleteState.UNCHECK
                            ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
                        }
                    }

                }
                R.id.iv_pause_play_music -> {
                    selectedViewHolder = this@ViewHolder
                    val audioFileView = listAudios.get(adapterPosition)

                    when (audioFileView.playerState) {
                        PlayerState.IDLE -> {
//                            selectedViewHolder!!.tvTotal.text = ""
//                            selectedViewHolder!!.tvTimeLife.text = ""
//                            selectedViewHolder!!.sbMusic.progress = 0
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

                R.id.iv_item_delete -> {
                    Log.d("001", "onClick: ssssssss")
                    val audioFileView = listAudios.get(adapterPosition)
                    when (audioFileView.deleteState) {

                        DeleteState.HIDE -> {
                        }
                        DeleteState.UNCHECK -> {
//                            val newAudioFileView = audioFileView.copy()
//                            newAudioFileView.deleteState = DeleteState.CHECKED
//                            listAudios.set(adapterPosition, newAudioFileView)
                            listAudios.get(adapterPosition).deleteState = DeleteState.CHECKED
                            ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)

                        }
                        DeleteState.CHECKED -> {
                            listAudios.get(adapterPosition).deleteState = DeleteState.UNCHECK
                            ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
                        }
                    }
                }

                R.id.iv_setting -> {
                    Log.d("001", "onClick: ")
                }
            }
        }


    }
}
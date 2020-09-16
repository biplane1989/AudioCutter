package com.example.audiocutter.functions.mystudio.audiocutter

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
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.functions.mystudio.MusicDiffCallBack
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import java.text.SimpleDateFormat
import java.util.concurrent.Executors

interface AudioCutterScreenCallback {
    fun play(audioFile: AudioFile)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Int)
    fun showMenu(view: View, audioFile: AudioFile)
    fun checkDeletePos(position: Int)
    fun isShowPlayingAudio(positition: Int)
}


class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) :
    ListAdapter<AudioFileView, AudioCutterAdapter.ViewHolder>(
        MusicDiffCallBack()
    ) {
    private var listAudios = ArrayList<AudioFileView>()
    private var selectedViewHolder: RecyclerView.ViewHolder? = null
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioCutterAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_screen_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioCutterAdapter.ViewHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val state = payloads.firstOrNull() as DeleteState
            when (state) {
                DeleteState.HIDE -> {

                    holder.ivItemDelete.visibility = View.GONE
                    holder.ivSetting.visibility = View.VISIBLE
                }
                DeleteState.UNCHECK -> {
                    holder.ivSetting.visibility = View.GONE
                    holder.ivItemDelete.visibility = View.VISIBLE
                    holder.ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
                }
                DeleteState.CHECKED -> {
                    holder.ivSetting.visibility = View.GONE
                    holder.ivItemDelete.visibility = View.VISIBLE
                    holder.ivItemDelete.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
                }
            }

        }

    }

    private fun updateItemView(audioFileView: AudioFileView, playerInfo: PlayerInfo) {
        if (selectedViewHolder != null) {
            if (audioFileView.playerState != playerInfo.playerState) {
                audioFileView.playerState = playerInfo.playerState
                when (audioFileView.playerState) {
                    PlayerState.PLAYING -> {
                        selectedViewHolder!!.itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_pause)
                    }
                    PlayerState.PAUSE -> {
                        selectedViewHolder!!.itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                    }
                    PlayerState.IDLE -> {
                        selectedViewHolder!!.itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                    }
                }
            }
            updateTimeSong(audioFileView, playerInfo)
        }
    }

    fun updateTimeSong(audioFileView: AudioFileView, playerInfo: PlayerInfo) {
        when (audioFileView.playerState) {
            PlayerState.PLAYING -> {

                Log.d("001", "updateTimeSong: position " + playerInfo.position)
                selectedViewHolder!!.itemView.tv_time_total.text =
                    "/" + simpleDateFormat.format(playerInfo.duration)
                selectedViewHolder!!.itemView.sb_music.max = playerInfo.duration
                selectedViewHolder!!.itemView.tv_time_life.text =
                    simpleDateFormat.format(playerInfo.position)
                selectedViewHolder!!.itemView.sb_music.progress = playerInfo.position
            }
            PlayerState.PAUSE -> {
            }
            PlayerState.IDLE -> {

                selectedViewHolder!!.itemView.tv_time_total.text = "/00:00"
                selectedViewHolder!!.itemView.tv_time_life.text = "00:00"
                selectedViewHolder!!.itemView.sb_music.progress = 0
            }
        }
    }

    override fun submitList(list: List<AudioFileView>?) {

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
        val ivItemDelete: ImageView = itemView.findViewById(R.id.iv_item_delete)
        val llAudioHeader: LinearLayout = itemView.findViewById(R.id.ll_audio_item_header)

        fun bind() {
            val audioFileView = getItem(adapterPosition)

            tvTitle.setText(audioFileView.audioFile.fileName)
            tvInfo.setText(audioFileView.audioFile.size.toString() + " MB" + " | " + audioFileView.audioFile.bitRate.toString() + "kb/s")

//            audioFileView.audioFile.file.absoluteFile?.let {
//                Glide.with(itemView.context).load(audioFileView.audioFile.file.absoluteFile)
//                    .transition(DrawableTransitionOptions.withCrossFade()).dontAnimate()
//                    .into(ivAvatar)
//            }

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

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            ivSetting.setOnClickListener(this)

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
                R.id.ll_audio_item_header -> {
                    val audioFileView = listAudios.get(adapterPosition)
                    selectedViewHolder = this@ViewHolder

                    audioCutterScreenCallback.stop()

                    when (audioFileView.deleteState) {
                        DeleteState.CHECKED -> {
                            audioCutterScreenCallback.checkDeletePos(adapterPosition)
                        }
                        DeleteState.UNCHECK -> {
                            audioCutterScreenCallback.checkDeletePos(adapterPosition)
                        }
                        DeleteState.HIDE -> {
                            audioCutterScreenCallback.isShowPlayingAudio(adapterPosition)
                        }
                    }
                }
                R.id.iv_pause_play_music -> {
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

                R.id.iv_setting -> {
                    Log.d("001", "onClick: ")
                    val audioFileView = listAudios.get(adapterPosition)
                    audioCutterScreenCallback.showMenu(view, audioFileView.audioFile)
                }
            }
        }
    }
}
package com.example.audiocutter.functions.mystudio.audiocutter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.functions.mystudio.MusicDiffCallBack
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import java.text.SimpleDateFormat

interface AudioCutterScreenCallback {
    fun play(position: Int)
    fun pause(position: Int)
    fun resume(position: Int)
    fun stop(position: Int)
    fun seekTo(cusorPos: Int)
    fun showMenu(view: View, audioFile: AudioFile)
    fun checkDeletePos(position: Int)
    fun isShowPlayingAudio(positition: Int)
}


class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) :
    ListAdapter<AudioFileView, AudioCutterAdapter.ViewHolder>(
        MusicDiffCallBack()
    ) {

    private val TAG = "giangtd"
    private var listAudios = ArrayList<AudioFileView>()
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
            val itemLoadStatus = payloads.firstOrNull() as ItemLoadStatus

            when (itemLoadStatus.deleteState) {
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
            val audioFileView = getItem(position)
            holder.tvTotal.text =
                "/" + simpleDateFormat.format(audioFileView.itemLoadStatus.duration)
            holder.sbMusic.max = audioFileView.itemLoadStatus.duration
            holder.tvTimeLife.text =
                simpleDateFormat.format(audioFileView.itemLoadStatus.currPos)
            holder.sbMusic.progress = audioFileView.itemLoadStatus.currPos
            when (itemLoadStatus.playerState) {
                PlayerState.PLAYING -> {
                    holder.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_pause)
                }
                PlayerState.PAUSE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                }
                PlayerState.IDLE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                    holder.tvTotal.text = Constance.DEFAULT_TOTAL_TIME
                    holder.tvTimeLife.text = Constance.DEFAULT_TIME_LIFE
                    holder.sbMusic.progress = Constance.DEFAULT_SEECKBAR
                }
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

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar_music)
        val ivSetting: ImageView = itemView.findViewById(R.id.iv_setting)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)

        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
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

            audioFileView.audioFile.file.absoluteFile?.let {
                Glide.with(itemView.context).load(audioFileView.audioFile.file.absoluteFile)
                    .transition(DrawableTransitionOptions.withCrossFade()).dontAnimate()
                    .into(ivAvatar)
            }

            if (audioFileView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
            } else {
                llPlayMusic.visibility = View.GONE
            }

            when (audioFileView.itemLoadStatus.deleteState) {

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

            when (audioFileView.itemLoadStatus.playerState) {
                PlayerState.PLAYING -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_pause)
                }
                PlayerState.PAUSE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_play)
                }
                PlayerState.IDLE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.output_audio_manager_screen_icon_play)

                    tvTotal.text = Constance.DEFAULT_TOTAL_TIME
                    tvTimeLife.text = Constance.DEFAULT_TIME_LIFE
                    sbMusic.progress = Constance.DEFAULT_SEECKBAR
                }
            }

            tvTotal.text =
                "/" + simpleDateFormat.format(audioFileView.itemLoadStatus.duration)
            sbMusic.max = audioFileView.itemLoadStatus.duration
            tvTimeLife.text =
                simpleDateFormat.format(audioFileView.itemLoadStatus.currPos)

            sbMusic.progress = audioFileView.itemLoadStatus.currPos

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

                    when (audioFileView.itemLoadStatus.deleteState) {
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


                    when (audioFileView.itemLoadStatus.playerState) {
                        PlayerState.IDLE -> {
                        }
                        PlayerState.PAUSE -> {
                            // khi ở trạng thái delete sẽ không stop dc
                            if (audioFileView.itemLoadStatus.deleteState == DeleteState.HIDE) {
                                audioCutterScreenCallback.stop(adapterPosition)
                            }
                        }
                        PlayerState.PLAYING -> {
                            if (audioFileView.itemLoadStatus.deleteState == DeleteState.HIDE) {
                                audioCutterScreenCallback.stop(adapterPosition)
                            }
                        }
                    }
                }
                R.id.iv_pause_play_music -> {
                    val audioFileView = listAudios.get(adapterPosition)

                    when (audioFileView.itemLoadStatus.playerState) {
                        PlayerState.IDLE -> {
//                            audioCutterScreenCallback.stop(adapterPosition)
                            audioCutterScreenCallback.play(adapterPosition)
                        }
                        PlayerState.PAUSE -> {
                            audioCutterScreenCallback.resume(adapterPosition)
                        }
                        PlayerState.PLAYING -> {
                            audioCutterScreenCallback.pause(adapterPosition)
                        }
                    }
                }

                R.id.iv_setting -> {
                    val audioFileView = listAudios.get(adapterPosition)
                    audioCutterScreenCallback.showMenu(view, audioFileView.audioFile)
                }
            }
        }
    }
}
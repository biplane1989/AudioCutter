package com.example.audiocutter.functions.mystudioscreen.fragment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudioscreen.AudioFileView
import com.example.audiocutter.functions.mystudioscreen.Constance
import com.example.audiocutter.functions.mystudioscreen.DeleteState
import com.example.audiocutter.functions.mystudioscreen.MusicDiffCallBack
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

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

class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) : ListAdapter<AudioFileView, AudioCutterAdapter.ViewHolder>(MusicDiffCallBack()) {

    private val TAG = "giangtd"
    private var listAudios = ArrayList<AudioFileView>()
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioCutterAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_screen_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioCutterAdapter.ViewHolder, position: Int) {
        holder.bind()
    }

    // khi chi thay doi 1 truong trong data
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
                    holder.ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
                }
                DeleteState.CHECKED -> {
                    holder.ivSetting.visibility = View.GONE
                    holder.ivItemDelete.visibility = View.VISIBLE
                    holder.ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_checked)
                }
            }
            val audioFileView = getItem(position)
            holder.tvTotal.text = "/" + simpleDateFormat.format(audioFileView.itemLoadStatus.duration)
            holder.sbMusic.max = audioFileView.itemLoadStatus.duration
            holder.tvTimeLife.text = simpleDateFormat.format(audioFileView.itemLoadStatus.currPos)
            holder.sbMusic.progress = audioFileView.itemLoadStatus.currPos

            when (itemLoadStatus.playerState) {
                PlayerState.PLAYING -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_pause)
                }
                PlayerState.PAUSE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                }
                PlayerState.IDLE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                    holder.tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                    holder.tvTotal.text = Constance.TIME_TOTAL_DEFAULT
                    holder.sbMusic.progress = 0
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

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
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        fun bind() {
            val audioFileView = getItem(adapterPosition)

            tvTitle.setText(audioFileView.audioFile.fileName)
            if (audioFileView.audioFile.size / (1024 * 1024) > 0) {

                tvInfo.setText(String.format("%.1f", (audioFileView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + audioFileView.audioFile.bitRate.toString() + "kb/s")
            } else {
                tvInfo.setText(((audioFileView.audioFile.size) / (1024)).toString() + " KB" + " | " + audioFileView.audioFile.bitRate.toString() + "kb/s")
            }

            audioFileView.audioFile.bitmap?.let {
                ivAvatar.setImageBitmap(it)
            }

            if (audioFileView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
                llItem.setBackgroundResource(R.drawable.my_studio_item_bg)
            } else {
                llPlayMusic.visibility = View.GONE
                llItem.setBackgroundColor(Color.WHITE)
            }

            when (audioFileView.itemLoadStatus.deleteState) {

                DeleteState.HIDE -> {
                    ivItemDelete.visibility = View.GONE
                    ivSetting.visibility = View.VISIBLE
                }
                DeleteState.UNCHECK -> {
                    ivSetting.visibility = View.GONE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
                }
                DeleteState.CHECKED -> {
                    ivSetting.visibility = View.GONE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_checked)
                }
            }

            when (audioFileView.itemLoadStatus.playerState) {
                PlayerState.PLAYING -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_pause)
                }
                PlayerState.PAUSE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)

                }
                PlayerState.IDLE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
                    tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                    tvTotal.text = Constance.TIME_TOTAL_DEFAULT
                    sbMusic.progress = 0
                }
            }

            tvTotal.text = "/" + simpleDateFormat.format(audioFileView.itemLoadStatus.duration)
            sbMusic.max = audioFileView.itemLoadStatus.duration
            tvTimeLife.text = simpleDateFormat.format(audioFileView.itemLoadStatus.currPos)

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
            val audioFileView = listAudios.get(adapterPosition)
            when (view.id) {

                R.id.ll_audio_item_header -> {
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
                    when (audioFileView.itemLoadStatus.playerState) {
                        PlayerState.IDLE -> {
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
                    audioCutterScreenCallback.showMenu(view, audioFileView.audioFile)
                }
            }
        }
    }
}
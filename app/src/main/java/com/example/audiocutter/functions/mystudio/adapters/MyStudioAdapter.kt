package com.example.audiocutter.functions.mystudio.adapters

import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    fun cancelLoading(id: Int)
}

class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback) : ListAdapter<AudioFileView, RecyclerView.ViewHolder>(MusicDiffCallBack()) {

    private val TAG = "giangtd"
    private var listAudios = ArrayList<AudioFileView>()
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SUCCESS_VIEW) {
            SuccessViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.my_studio_screen_item, parent, false))
        } else LoadingViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_item_loading, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).convertingState == ConvertingState.SUCCESS) {
            SUCCESS_VIEW
        } else {
            LOADING_VIEW
        }
    }

    override fun submitList(list: List<AudioFileView>?) {
        if (list != null) {
            listAudios = ArrayList(list)
            super.submitList(ArrayList(list))
        } else {
            listAudios = ArrayList()
            super.submitList(null)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (SUCCESS_VIEW == getItemViewType(position)) {
            val successViewHolder = holder as SuccessViewHolder
            successViewHolder.onBind()
        } else {
            val loadingViewHolder = holder as LoadingViewHolder
            loadingViewHolder.onBind()
        }
    }

    // khi chi thay doi 1 truong trong data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (holder is SuccessViewHolder) {
                val successViewHolder = holder as SuccessViewHolder
                val newItem = payloads.firstOrNull() as AudioFileView

                when (newItem.itemLoadStatus.deleteState) {
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

                if (newItem.isExpanded) {
                    holder.llPlayMusic.visibility = View.VISIBLE
                    holder.llItem.setBackgroundResource(R.drawable.my_studio_item_bg)
                } else {
                    holder.llPlayMusic.visibility = View.GONE
                    holder.llItem.setBackgroundColor(Color.WHITE)
//                    holder.llItem.setBackgroundResource(R.drawable.bg_white_rec)
                    holder.llItem.setPadding(0, 0, 0, 0)
                }
                holder.sbMusic.max = newItem.itemLoadStatus.duration
                holder.tvTimeLife.text = simpleDateFormat.format(newItem.itemLoadStatus.currPos)
                holder.sbMusic.progress = newItem.itemLoadStatus.currPos

                when (newItem.itemLoadStatus.playerState) {
                    PlayerState.PLAYING -> {
                        holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_pause)
                    }
                    PlayerState.PAUSE -> {
                        holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                    }
                    PlayerState.IDLE -> {
                        holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                        holder.tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                        holder.sbMusic.progress = 0
                    }
                }
            } else {
                val loadingViewHolder = holder as LoadingViewHolder
                val newItem = payloads.firstOrNull() as AudioFileView
                val convertingItem = getItem(position)

                loadingViewHolder.pbLoading.max = 100
                loadingViewHolder.pbLoading.progress = convertingItem.percent

                loadingViewHolder.tvTitle.setText(convertingItem.audioFile.fileName)

                loadingViewHolder.tvLoading.text = newItem.percent.toString() + "%"
            }
        }
    }

    inner class SuccessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

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
        fun onBind() {
            val audioFileView = getItem(adapterPosition)
            var bitrate = audioFileView.audioFile.bitRate / 1000
            if (bitrate > 320) {
                bitrate = 320
            }
            tvTitle.setText(audioFileView.audioFile.fileName)
            if (audioFileView.audioFile.size / (1024 * 1024) > 0) {

                tvInfo.setText(String.format("%.1f", (audioFileView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + (audioFileView.audioFile.bitRate / 1000).toString() + "kb/s")
            } else {
                tvInfo.setText(((audioFileView.audioFile.size) / (1024)).toString() + " KB" + " | " + (audioFileView.audioFile.bitRate / 1000).toString() + "kb/s")
            }

            tvTotal.text = "/" + simpleDateFormat.format(audioFileView.audioFile.time?.toInt())
            audioFileView.audioFile.bitmap?.let {
                ivAvatar.setImageBitmap(it)
            }

            if (audioFileView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
                llItem.setBackgroundResource(R.drawable.my_studio_item_bg)
            } else {
                llPlayMusic.visibility = View.GONE
                llItem.setBackgroundColor(Color.WHITE)
                llItem.setPadding(0, 0, 0, 0)
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
                    sbMusic.progress = 0
                }
            }

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

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        val ivCancel: ImageView = itemView.findViewById(R.id.iv_cancel)
        val tvWait: TextView = itemView.findViewById(R.id.tv_wait)

        fun onBind() {
            val loadingItem = getItem(adapterPosition)

            when (loadingItem.convertingState) {
                ConvertingState.WAITING -> {
                    tvWait.visibility = View.VISIBLE
                    pbLoading.visibility = View.GONE
                    tvLoading.visibility = View.GONE
                }
                ConvertingState.PROGRESSING -> {
                    tvWait.visibility = View.GONE
                    pbLoading.visibility = View.VISIBLE
                    tvLoading.visibility = View.VISIBLE
                    pbLoading.max = 100
                    pbLoading.progress = loadingItem.percent
                    tvLoading.text = loadingItem.percent.toString() + "%"
                }
            }

            tvTitle.setText(loadingItem.audioFile.fileName)

            ivCancel.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val loadingItem = listAudios.get(adapterPosition)
            when (view.id) {
                R.id.iv_cancel -> {
                    audioCutterScreenCallback.cancelLoading(loadingItem.id)
                    Log.d(TAG, "onClick: ")
                }
            }
        }
    }

    companion object {
        const val SUCCESS_VIEW = 0
        const val LOADING_VIEW = 1
    }
}

class MusicDiffCallBack : DiffUtil.ItemCallback<AudioFileView>() {

    override fun areItemsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView.audioFile.file.absoluteFile == newItemView.audioFile.file.absoluteFile
    }

    override fun areContentsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: AudioFileView, newItem: AudioFileView): Any? {

        return newItem
    }
}
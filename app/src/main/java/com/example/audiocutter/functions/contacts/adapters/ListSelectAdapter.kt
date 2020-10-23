package com.example.audiocutter.functions.contacts.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.mystudio.Constance
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import java.text.SimpleDateFormat

interface SelectAudioScreenCallback {
    fun play(position: Int)
    fun pause()
    fun resume()
    fun stop(position: Int)
    fun seekTo(cusorPos: Int)
    fun isShowPlayingAudio(positition: Int)
    fun isSelect(position: Int)
}

class ListSelectAdapter(var selectAudioScreenCallback: SelectAudioScreenCallback) : ListAdapter<SelectItemView, ListSelectAdapter.ViewHolder>(SelectAudioDiffCallBack()) {
    private val TAG = "giangtd"
    private var listAudios = ArrayList<SelectItemView>()
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact_select_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    // khi chỉ thay đổi 1 phần trên ui
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val newItem = payloads.firstOrNull() as SelectItemView
            val selectItemView = getItem(position)
//            holder.tvTotal.text = "/" + simpleDateFormat.format(selectItemView.selectItemStatus.duration)
            holder.sbMusic.max = selectItemView.selectItemStatus.duration
            holder.tvTimeLife.text = simpleDateFormat.format(selectItemView.selectItemStatus.currPos)
            holder.sbMusic.progress = selectItemView.selectItemStatus.currPos

            when (newItem.selectItemStatus.playerState) {
                PlayerState.PLAYING -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_pause)
                }
                PlayerState.PAUSE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                }
                PlayerState.IDLE -> {
                    holder.ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                    holder.tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
//                    holder.tvTotal.text = Constance.TIME_TOTAL_DEFAULT
                    holder.sbMusic.progress = 0
                }
            }
            if (newItem.isSelect) {
                holder.ivSelect.setImageResource(R.drawable.list_contact_select)
            } else {
                holder.ivSelect.setImageResource(R.drawable.list_contact_unselect)
            }
        }
    }

    override fun submitList(list: List<SelectItemView>?) {

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
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)

        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
        val ivPausePlay: ImageView = itemView.findViewById(R.id.iv_pause_play_music)
        val sbMusic: SeekBar = itemView.findViewById(R.id.sb_music)
        val tvTimeLife: TextView = itemView.findViewById(R.id.tv_time_life)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_time_total)
        val llAudioHeader: LinearLayout = itemView.findViewById(R.id.ll_audio_item_header)
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        val ivSelect: ImageView = itemView.findViewById(R.id.iv_select)
        val cvCarview: CardView = itemView.findViewById(R.id.cv_default)

        fun bind() {
            val selectItemView = getItem(adapterPosition)

            tvTitle.setText(selectItemView.audioFile.fileName)
            if (selectItemView.audioFile.size / (1024 * 1024) > 0) {

                tvInfo.setText(String.format("%.1f", (selectItemView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + selectItemView.audioFile.bitRate.toString() + "kb/s")
            } else {
                tvInfo.setText(((selectItemView.audioFile.size) / (1024)).toString() + " KB" + " | " + selectItemView.audioFile.bitRate.toString() + "kb/s")
            }

            selectItemView.audioFile.bitmap?.let {
                ivAvatar.setImageBitmap(it)
            }

            if (selectItemView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
                llItem.setBackgroundResource(R.drawable.list_contact_select_item_bg)
            } else {
                llPlayMusic.visibility = View.GONE
                llItem.setBackgroundColor(Color.WHITE)
            }

            if (selectItemView.isSelect) {
                ivSelect.setImageResource(R.drawable.list_contact_select)
            } else {
                ivSelect.setImageResource(R.drawable.list_contact_unselect)
            }

            if (selectItemView.isRingtoneDefault) {
                cvCarview.visibility = View.VISIBLE
            } else {
                cvCarview.visibility = View.GONE
            }

            when (selectItemView.selectItemStatus.playerState) {
                PlayerState.PLAYING -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_pause)
                }
                PlayerState.PAUSE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)

                }
                PlayerState.IDLE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
                    tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
//                    tvTotal.text = Constance.TIME_TOTAL_DEFAULT
                    sbMusic.progress = 0
                }
            }

//            tvTotal.text = "/" + simpleDateFormat.format(selectItemView.selectItemStatus.duration)

            sbMusic.max = selectItemView.selectItemStatus.duration
            tvTimeLife.text = simpleDateFormat.format(selectItemView.selectItemStatus.currPos)

            sbMusic.progress = selectItemView.selectItemStatus.currPos

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    selectAudioScreenCallback.seekTo(sbMusic.progress)
                }
            })
        }

        override fun onClick(view: View) {
            val audioFileView = listAudios.get(adapterPosition)
            when (view.id) {
                R.id.ll_audio_item_header -> {
                    selectAudioScreenCallback.isShowPlayingAudio(adapterPosition)
                    selectAudioScreenCallback.isSelect(adapterPosition)
                    tvTotal.text = "/" + simpleDateFormat.format(audioFileView.duration.toInt())
                }
                R.id.iv_pause_play_music -> {

                    when (audioFileView.selectItemStatus.playerState) {
                        PlayerState.IDLE -> {
                            selectAudioScreenCallback.play(adapterPosition)
                        }
                        PlayerState.PAUSE -> {
                            selectAudioScreenCallback.resume()
                        }
                        PlayerState.PLAYING -> {
                            selectAudioScreenCallback.pause()
                        }
                    }
                }
            }
        }
    }
}

class SelectAudioDiffCallBack : DiffUtil.ItemCallback<SelectItemView>() {

    override fun areItemsTheSame(oldItemView: SelectItemView, newItemView: SelectItemView): Boolean {
        return oldItemView.audioFile.file.absoluteFile == newItemView.audioFile.file.absoluteFile
    }

    override fun areContentsTheSame(oldItemView: SelectItemView, newItemView: SelectItemView): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: SelectItemView, newItem: SelectItemView): Any? {
        return newItem
    }
}
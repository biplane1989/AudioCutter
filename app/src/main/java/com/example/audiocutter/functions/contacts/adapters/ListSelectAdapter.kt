package com.example.audiocutter.functions.contacts.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.Util
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.ui.common.glide.RoundedCornersTransformation
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

interface SelectAudioScreenCallback {
    fun isShowPlayingAudio(positition: Int)
}

class ListSelectAdapter(var selectAudioScreenCallback: SelectAudioScreenCallback, val audioPlayer: AudioPlayer, val lifecycleCoroutineScope: LifecycleCoroutineScope) : ListAdapter<SelectItemView, ListSelectAdapter.ViewHolder>(SelectAudioDiffCallBack()) {
    private val TAG = "giangtd"

    private lateinit var recyclerView: RecyclerView
    private var option = Options()


    @SuppressLint("SimpleDateFormat")
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        recyclerView = parent as RecyclerView
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

            if (newItem.isExpanded) {
                holder.llPlayMusic.visibility = View.VISIBLE
                holder.llItem.setBackgroundResource(R.drawable.list_contact_select_item_bg)

                Log.d(TAG, "onBindViewHolder: 1")
                holder.itemView.post {                      // khi nhan item cuoi cung ma bi che khuat --> tu dong day item do len
                    if (holder.itemView.bottom > recyclerView.height) {
                        recyclerView.smoothScrollBy(0, (holder.itemView.bottom - recyclerView.height))
                    }
                }
            } else {
                holder.llPlayMusic.visibility = View.GONE
                holder.llItem.setBackgroundColor(Color.WHITE)
                holder.llItem.setPadding(0, 0, 0, 0)
            }

            if (newItem.isSelect) {
                holder.ivSelect.setImageResource(R.drawable.list_contact_select)
            } else {
                holder.ivSelect.setImageResource(R.drawable.list_contact_unselect)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {       // khi view dc hien thi tren man hinh
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()

    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {     // khi view bi destroy tren man hinh
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }


    override fun submitList(list: List<SelectItemView>?) {
        if (list != null) {
            super.submitList(ArrayList(list))
        } else {
            super.submitList(null)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LifecycleOwner {

        val ivAvatarSelect: ImageView = itemView.findViewById(R.id.cv_avatar)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)
        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
        val ivPausePlay: ImageView = itemView.findViewById(R.id.iv_pause_play_music)
        val sbMusic: SeekBar = itemView.findViewById(R.id.sb_music)
        val tvTimeLife: TextView = itemView.findViewById(R.id.tv_time_life)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_time_total)

        //        val llAudioHeader: LinearLayout = itemView.findViewById(R.id.ll_audio_item_header)
        val llAudioHeader: ConstraintLayout = itemView.findViewById(R.id.ll_audio_item_header)
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        val ivSelect: ImageView = itemView.findViewById(R.id.iv_select)
        val cvCarview: CardView = itemView.findViewById(R.id.cv_default)

        private var isSeekBarStatus = false         // trang thai seekbar co dang duoc keo hay khong

        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)      // tao 1 lifecycleRegistry
        var playerState: PlayerState = PlayerState.IDLE

        override fun getLifecycle(): Lifecycle {                                                // gan lifecycleRegistry tu dinh nghia cho class
            return lifecycleRegistry
        }

        private fun updatePlayInfor(playerInfo: PlayerInfo) {

            playerState = playerInfo.playerState
            sbMusic.max = playerInfo.duration
            sbMusic.progress = playerInfo.posision
            tvTimeLife.text = simpleDateFormat.format(playerInfo.posision)

            when (playerInfo.playerState) {
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
                else -> {
                    //nothing
                }
            }
        }

        fun onViewAttachedToWindow() {                                                // gan view vao trong windows do minh tu viet
            lifecycleRegistry.currentState = Lifecycle.State.STARTED                  // livedata o trang thai is Active thi moi hoat dong STARTED or RESUMED
            audioPlayer.getPlayerInfo().observe(this, object : Observer<PlayerInfo> {
                override fun onChanged(playerInfo: PlayerInfo) {
                    playerInfo.currentAudio?.let {
                        if (!isSeekBarStatus && adapterPosition != -1) {                            // khi summitlist: ham onViewDetachedFromWindow() vua chay va  audioPlayer.getPlayerInfo().observe cung chay nen adapterPosition = -1 (chua kip lay data)
                            val selectItemView = getItem(adapterPosition)
                            if (selectItemView.getFilePath() == it.getFilePath()) {
                                updatePlayInfor(playerInfo)
                            }
                        }
                    }
                }
            })
        }

        fun onViewDetachedFromWindow() {                                                // go~ view ra khoi windows do minh tu viet
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val selectItemView = getItem(adapterPosition)

            tvTitle.setText(selectItemView.audioFile.fileName)

            if (selectItemView.audioFile.size / (1024 * 1024) > 0) {
                tvInfo.setText(String.format("%.1f", (selectItemView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + (selectItemView.audioFile.bitRate / 1000).toString() + "kb/s")
            } else {
                tvInfo.setText(((selectItemView.audioFile.size) / (1024)).toString() + " KB" + " | " + (selectItemView.audioFile.bitRate / 1000).toString() + "kb/s")
            }

            if (selectItemView.audioFile.bitmap != null) {
                Glide.with(itemView).load(selectItemView.audioFile.bitmap)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(recyclerView.context, Utils.convertDp2Px(8, recyclerView.context).toInt(), Utils.convertDp2Px(0, recyclerView.context).toInt())))
                    .into(ivAvatarSelect)
            } else {
                ivAvatarSelect.setImageResource(R.drawable.my_studio_item_ic_avatar)
            }



            tvTimeLife.width = Utils.getWidthText(simpleDateFormat.format(selectItemView.audioFile.duration), itemView.context)
                .toInt() + 50

            tvTotal.text = "/" + simpleDateFormat.format(selectItemView.audioFile.duration.toInt())

            if (selectItemView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
                llItem.setBackgroundResource(R.drawable.list_contact_select_item_bg)
            } else {
                llPlayMusic.visibility = View.GONE
                llItem.setBackgroundColor(Color.WHITE)
                llItem.setPadding(0, 0, 0, 0)
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

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    tvTimeLife.text = simpleDateFormat.format(sbMusic.progress)             // update time cho tvTimeLife khi keo seekbar
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    isSeekBarStatus = true
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    audioPlayer.seek(sbMusic.progress)
                    isSeekBarStatus = false
                }
            })
        }

        override fun onClick(view: View) {
            val selectItemView = getItem(adapterPosition)
            when (view.id) {
                R.id.ll_audio_item_header -> {
                    playerState = PlayerState.IDLE
                    audioPlayer.stop()
                    selectAudioScreenCallback.isShowPlayingAudio(adapterPosition)
                }
                R.id.iv_pause_play_music -> {
                    when (playerState) {
                        PlayerState.IDLE -> {
                            lifecycleCoroutineScope.launch {
                                audioPlayer.play(selectItemView.audioFile)
                            }

                        }
                        PlayerState.PAUSE -> {
                            audioPlayer.resume()
                        }
                        PlayerState.PLAYING -> {
                            audioPlayer.pause()
                        }
                        else -> {
                            //nothing
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
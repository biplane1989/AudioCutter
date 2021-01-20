package com.example.audiocutter.functions.contacts.adapters

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*

interface SelectAudioScreenCallback {
    fun isShowPlayingAudio(filePath: String)
}

class ListSelectAdapter(var selectAudioScreenCallback: SelectAudioScreenCallback, val audioPlayer: AudioPlayer, val lifecycleCoroutineScope: LifecycleCoroutineScope) : ListAdapter<SelectItemView, ListSelectAdapter.ViewHolder>(SelectAudioDiffCallBack()) {

    private val TAG = "giangtd"
    private lateinit var recyclerView: RecyclerView

    private var sbAnimation: ObjectAnimator? = null

    private val DURATION_ANIMATION = 500L

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
            val data = payloads.firstOrNull()

            data?.let {
                val newItem = data as SelectItemView
                if (newItem.isExpanded) {
                    holder.llPlayMusic.visibility = View.VISIBLE
                    holder.llItem.setBackgroundResource(R.drawable.list_contact_select_item_bg)

                    Log.d(TAG, "onBindViewHolder: 1")
                    holder.itemView.post {                      // khi nhan item cuoi cung ma bi che khuat --> tu dong day item do len
                        if (holder.itemView.bottom > recyclerView.height) {
                            recyclerView.smoothScrollBy(0, (holder.itemView.bottom - recyclerView.height))
                        }
                    }
                    holder.sbMusic.clearAnimation()
                    sbAnimation?.cancel()

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

                if (newItem.isRingtoneDefault) {
                    holder.tvRingtoneDefault.visibility = View.VISIBLE
                } else {
                    holder.tvRingtoneDefault.visibility = View.GONE
                }

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LifecycleOwner {

        val ivAvatarSelect: ImageView = itemView.findViewById(R.id.cv_avatar)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)
        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
        val ivPausePlay: ImageView = itemView.findViewById(R.id.iv_pause_play_music)
        val sbMusic: SeekBar = itemView.findViewById(R.id.sb_music)
        val tvTimeLife: TextView = itemView.findViewById(R.id.tv_time_life)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_time_total)
        val llAudioHeader: ConstraintLayout = itemView.findViewById(R.id.ll_audio_item_header)
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
        val ivSelect: ImageView = itemView.findViewById(R.id.iv_select)
        val tvRingtoneDefault: TextView = itemView.findViewById(R.id.tv_default_song_ringtone)

        private var timeFomat = 0
        private var isFirstPlayMusic = true
        private var isSeekBarStatus = false         // trang thai seekbar co dang duoc keo hay khong

        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)      // tao 1 lifecycleRegistry
        var playerState: PlayerState = PlayerState.IDLE

        override fun getLifecycle(): Lifecycle {                                                // gan lifecycleRegistry tu dinh nghia cho class
            return lifecycleRegistry
        }

        private fun updatePlayInfor(playerInfo: PlayerInfo) {
            val audioFileView = getItem(adapterPosition)
            if (playerInfo.currentAudio?.getFilePath()
                    .equals(audioFileView.audioFile.getFilePath())) {

                if (!audioFileView.isExpanded) {                        // fix bug nhan play va 1 item khac. nhac se chay khi item phat nhac bi an
                    audioPlayer.stop()
                }

                timeFomat = Utils.chooseTimeFormat(playerInfo.duration.toLong())
                tvTimeLife.text = Utils.toTimeStr(playerInfo.posision.toLong(), timeFomat)

                when (playerInfo.playerState) {

                    PlayerState.PLAYING -> {
                        Log.d(TAG, "onProgressChanged: aloha progress pecent : " + playerInfo.posision)
                        itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_pause)
                        if (isFirstPlayMusic) {
                            sbMusic.progress = playerInfo.posision * 100
                        } else {
                            setSeekbarAnimate(sbMusic, playerInfo.posision, DURATION_ANIMATION)
                        }

                    }
                    PlayerState.PAUSE -> {
                        itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)

                    }
                    PlayerState.IDLE -> {
                        tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                        resetItemView()
                        setSeekbarAnimate(sbMusic, 0, DURATION_ANIMATION)
                    }
                    else -> {
                        //nothing
                    }
                }
                isFirstPlayMusic = false
            }
        }

        private fun setSeekbarAnimate(pb: SeekBar, progressTo: Int, duration: Long) {
            // smooth animation

            Log.d(TAG, "setSeekbarAnimate: pb.progress : " + progressTo * 100 + " max ${pb.max} current ${pb.progress}")
            sbAnimation?.cancel()
            sbAnimation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
            sbAnimation?.setDuration(duration)
            sbAnimation?.setInterpolator(DecelerateInterpolator())
            sbAnimation?.start()
        }

        fun onViewAttachedToWindow() {                                                // gan view vao trong windows do minh tu viet
            resetAnimation()
//            sbMusic.progress = 0
            isFirstPlayMusic = true
            lifecycleRegistry.currentState = Lifecycle.State.STARTED                  // livedata o trang thai is Active thi moi hoat dong STARTED or RESUMED
            audioPlayer.getPlayerInfo().observe(this, object : Observer<PlayerInfo> {
                override fun onChanged(playerInfo: PlayerInfo) {
                    playerState = playerInfo.playerState
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
            sbAnimation?.cancel()
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val selectItemView = getItem(adapterPosition)

            sbMusic.max = selectItemView.audioFile.duration.toInt() * 100

            tvTitle.setText(selectItemView.audioFile.fileName)

            if (selectItemView.audioFile.size / (1024 * 1024) > 0) {
                tvInfo.setText(String.format("%.1f", (selectItemView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + (selectItemView.audioFile.bitRate / 1000).toString() + "kb/s")
            } else {
                tvInfo.setText(((selectItemView.audioFile.size) / (1024)).toString() + " KB" + " | " + (selectItemView.audioFile.bitRate / 1000).toString() + "kb/s")
            }


            timeFomat = Utils.chooseTimeFormat(selectItemView.audioFile.duration)

            tvTimeLife.width = tvTimeLife.paint.measureText(Utils.toTimeStr(selectItemView.audioFile.duration, timeFomat))
                .toInt()
            tvTotal.width = tvTotal.paint.measureText("/" + Utils.toTimeStr(selectItemView.audioFile.duration, timeFomat))
                .toInt()

            tvTotal.text = "/" + Utils.toTimeStr(selectItemView.audioFile.duration, timeFomat)

            if (selectItemView.audioFile.bitmap != null) {
                Glide.with(itemView).load(selectItemView.audioFile.bitmap).into(ivAvatarSelect)

            } else {
                ivAvatarSelect.setImageResource(R.drawable.my_studio_item_ic_avatar)
            }

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
                tvRingtoneDefault.visibility = View.VISIBLE
            } else {
                tvRingtoneDefault.visibility = View.GONE
            }

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {

                    Log.d(TAG, "onProgressChanged: aloha progress : " + progress)
                    if (playerState != PlayerState.IDLE) {
                        if (fromUser) {
                            resetAnimation()
                            setSeekbarAnimate(sbMusic, progress / 100, DURATION_ANIMATION)
                        }
                    }
                    if (playerState != PlayerState.PLAYING) {
                        tvTimeLife.text = Utils.toTimeStr(progress.toLong() / 100, timeFomat)
                    }

                    if (progress == sbMusic.max) {
                        Log.d(TAG, "onProgressChanged: progress == max ")
                        resetItemView()
                        sbMusic.progress = 0
                        playerState = PlayerState.IDLE
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {
                    if (playerState == PlayerState.PLAYING) {
                        audioPlayer.pause()
                        resetAnimation()
                        isSeekBarStatus = true
                    }
                }

                override fun onStopTrackingTouch(sb: SeekBar?) {
                    if (playerState == PlayerState.IDLE) {
//                        lifecycleScope.launch {
                        resetAnimation()
                        val newValue = Utils.convertValue(0, sbMusic.max, 0, selectItemView.audioFile.duration.toInt(), sbMusic.progress)
                        audioPlayer.play(selectItemView.audioFile, newValue)
//                        }
                    }
                    resetAnimation()
                    audioPlayer.seek(sbMusic.progress / 100, true)
                    isSeekBarStatus = false
                }

            })  // sb

        }

        private fun resetAnimation() {
            sbMusic.clearAnimation()
            sbAnimation?.cancel()
        }

        private fun resetItemView() {
            resetAnimation()
            sbMusic.progress = 0
            itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
        }

        override fun onClick(view: View) {
            val selectItemView = getItem(adapterPosition)
            when (view.id) {
                R.id.ll_audio_item_header -> {
                    resetItemView()
                    playerState = PlayerState.IDLE
                    audioPlayer.stop()
                    selectAudioScreenCallback.isShowPlayingAudio(selectItemView.getFilePath())
                }
                R.id.iv_pause_play_music -> {
                    when (playerState) {
                        PlayerState.IDLE -> {
//                            lifecycleCoroutineScope.launch {
                            audioPlayer.play(selectItemView.audioFile)
                            resetAnimation()
                            sbMusic.progress = 0
                            setSeekbarAnimate(sbMusic, 0, DURATION_ANIMATION)
//                            }
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
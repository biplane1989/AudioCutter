package com.example.audiocutter.functions.audiochooser.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.ui.audiochooser.cut.ProgressView
import com.example.audiocutter.ui.audiochooser.cut.WaveAudio
import kotlinx.coroutines.launch

class MergeChooserAdapter(
    val mContext: Context,
    val audioPlayer: AudioPlayer,
    val lifecycleCoroutineScope: LifecycleCoroutineScope,
    val activity: Activity

) : ListAdapter<AudioCutterViewItem, MergeChooserAdapter.MergeHolder>(MergerChooserAudioDiff()) {
    lateinit var mCallBack: AudioMergeListener
    val SIZE_MB = 1024 * 1024
    fun setAudioListener(event: AudioMergeListener) {
        mCallBack = event
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_merger, parent, false)
        return MergeHolder(view)
    }

    override fun onBindViewHolder(holder: MergeHolder, position: Int) {
        holder.bind()
    }

    override fun onViewAttachedToWindow(holder: MergeHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: MergeHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(holder: MergeHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val itemAudioFile = getItem(position)
            val diff = payloads.get(0) as Bundle

            if (diff.containsKey(ITEM_CHECKBOX_CHANGED)) {
                when (itemAudioFile.isCheckChooseItem) {
                    true -> holder.ivChecked.setImageResource(R.drawable.ic_checkdone)
                    false -> holder.ivChecked.setImageResource(R.drawable.ic_noncheck)
                }
            }
            if (diff.containsKey(ITEM_PROGRESSING_CHANGED)) {
                holder.pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration, true)
            }
            if (diff.containsKey(ITEM_PLAY_BUTTON_CHANGED)) {
                val bitmap = itemAudioFile.audioFile.bitmap
                updateItem(itemAudioFile.state, holder, bitmap)
            }


        }
    }

    fun updateItem(status: PlayerState, holder: MergeHolder, bitmap: Bitmap?) {
        when (status) {
            PlayerState.PLAYING -> {
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                        .into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_play_default)
                }
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_play)
                holder.pgAudio.visibility = View.VISIBLE
                holder.waveView.visibility = View.VISIBLE
            }
            PlayerState.PAUSE -> {
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                        .into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                }
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                holder.waveView.visibility = View.INVISIBLE
                holder.pgAudio.visibility = View.VISIBLE
            }
            PlayerState.IDLE -> {
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                        .into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                }
                holder.pgAudio.visibility = View.GONE
                holder.waveView.visibility = View.INVISIBLE
                holder.pgAudio.resetView()
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
            }
            else -> {
                //nothing
            }
        }
    }
    inner class MergeHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, LifecycleOwner {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio_merger)
        val ivChecked = itemView.findViewById<ImageView>(R.id.iv_merger_check)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio_merger)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio_merger)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio_merger)
        val lnItem = itemView.findViewById<LinearLayout>(R.id.ln_item_audio_merger_screen)
        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu_merger)
        val pgAudio = itemView.findViewById<ProgressView>(R.id.pg_audio_merger_screen)
        val waveView = itemView.findViewById<WaveAudio>(R.id.wave_audio_merger)
        val ivPausePlay = itemView.findViewById<ImageView>(R.id.iv_pause_play_audio)

        init {
            ivController.setOnClickListener(this)
            lnMenu.setOnClickListener(this)
            lnItem.setOnClickListener(this)
        }

        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        var playerState: PlayerState = PlayerState.IDLE

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        private fun updatePlayInfor(playerInfo: PlayerInfo) {
            playerState = playerInfo.playerState
            pgAudio.updatePG(playerInfo.posision.toLong(), playerInfo.duration.toLong())

            val itemAudioFile = getItem(adapterPosition)
            val bitmap = itemAudioFile.audioFile.bitmap
            itemAudioFile.state = playerInfo.playerState
            itemAudioFile.currentPos = playerInfo.posision.toLong()
            itemAudioFile.duration = playerInfo.duration.toLong()

            updateItem(playerInfo.playerState, this, bitmap)
        }

        fun onViewAttachedToWindow() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            audioPlayer.getPlayerInfo().observe(this, object : Observer<PlayerInfo> {
                override fun onChanged(playerInfo: PlayerInfo) {
                    playerInfo.currentAudio?.let {
                        if (adapterPosition != -1) {
                            val audioCutterView = getItem(adapterPosition)
                            if (audioCutterView.audioFile.getFilePath() == it.getFilePath()) {
                                updatePlayInfor(playerInfo)
                            } else {
                                resetItem(audioCutterView)
                            }
                        }
                    }
                }
            })
        }

        fun resetItem(audioCutterView: AudioCutterViewItem) {

            playerState = PlayerState.IDLE
            pgAudio.visibility = View.GONE
            waveView.visibility = View.INVISIBLE
            if (checkValidGlide(audioCutterView.audioFile.bitmap)) {
                Glide.with(itemView).load(audioCutterView.audioFile.bitmap).into(ivController)
            } else {
                ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
            }
            ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
        }

        fun onViewDetachedFromWindow() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val itemAudioFile = getItem(adapterPosition)
            var bitRate = itemAudioFile.audioFile.bitRate / 1000

            tvBitrateAudio.text = " | ${bitRate}${mContext.resources.getString(R.string.kbps)}"

            tvNameAudio.text = itemAudioFile.audioFile.fileName
            var size = (itemAudioFile.audioFile.size.toDouble() / SIZE_MB)

            if (size >= 1) {
                size = Math.floor(size * 10) / 10
                tvSizeAudio.text = "$size ${mContext.resources.getString(R.string.megabyte)}"
            } else {
                size = (itemAudioFile.audioFile.size.toDouble() / 1024)
                size = Math.floor(size * 10) / 10
                tvSizeAudio.text = "$size ${mContext.resources.getString(R.string.kilobyte)}"
            }

            if (itemAudioFile.currentPos > 0) {
                pgAudio.post {
                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration, false)
                }

            }
            val bitmap = itemAudioFile.audioFile.bitmap

            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                            .into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_play_default)
                    }
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_play)
                    pgAudio.visibility = View.VISIBLE
                    waveView.visibility = View.VISIBLE

                }
                PlayerState.PAUSE -> {
                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                            .into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                    }
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                    waveView.visibility = View.INVISIBLE
                    pgAudio.visibility = View.VISIBLE
                }
                PlayerState.IDLE -> {
                    pgAudio.visibility = View.GONE
                    waveView.visibility = View.INVISIBLE

                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                            .into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                    }
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                }
                else -> {
                    //nothing
                }
            }


            when (itemAudioFile.isCheckChooseItem) {
                true -> ivChecked.setImageResource(R.drawable.ic_checkdone)
                false -> ivChecked.setImageResource(R.drawable.ic_noncheck)
            }

        }


        override fun onClick(p0: View) {
            val item = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio_merger -> controllerAudio()
                R.id.ln_menu_merger -> {
                    checkItem()
//                    mCallBack.chooseItemAudio(getItem(adapterPosition), item.isCheckChooseItem)
                }
                R.id.ln_item_audio_merger_screen -> {
                    checkItem()
//                    mCallBack.chooseItemAudio(getItem(adapterPosition), item.isCheckChooseItem)
                }
            }
        }

        private fun checkItem() {
            mCallBack.chooseItemAudio(adapterPosition)
        }

        private fun controllerAudio() {
            val itemAudio = getItem(adapterPosition)
            if (adapterPosition == -1) {
                return
            }
            when (playerState) {
                PlayerState.IDLE -> {
                    pgAudio.resetView()
                    lifecycleCoroutineScope.launch {
                        pgAudio.resetView()
                        audioPlayer.play(itemAudio.audioFile)
                    }
//                    mCallBack.play(adapterPosition)
                }
                PlayerState.PAUSE -> {
                    audioPlayer.resume()
//                    mCallBack.resume(adapterPosition)
                }
                PlayerState.PLAYING -> {
                    audioPlayer.pause()
//                    mCallBack.pause(adapterPosition)
                }
                else -> {

                }
            }
        }
    }

    private fun checkValidGlide(bitmap: Bitmap?): Boolean {
        return (bitmap != null && !activity.isFinishing)
    }

    interface AudioMergeListener {
        fun chooseItemAudio(position: Int)
    }
}



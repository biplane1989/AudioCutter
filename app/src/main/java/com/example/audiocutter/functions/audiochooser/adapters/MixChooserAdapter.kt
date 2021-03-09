package com.example.audiocutter.functions.audiochooser.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
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
class MixChooserAdapter(
    val mContext: Context,
    val audioPlayer: AudioPlayer,
    val lifecycleCoroutineScope: LifecycleCoroutineScope,
    val activity: Activity
) : ListAdapter<AudioCutterViewItem, MixChooserAdapter.RecentHolder>(MixChooserAudioDiff()) {
    lateinit var mCallBack: AudioMixerListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterViewItem>()


    fun setAudioCutterListtener(event: AudioMixerListener) {
        mCallBack = event
    }

    override fun submitList(list: MutableList<AudioCutterViewItem>?, commitCallback: Runnable?) {
        if (list!!.size != 0 || list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios, commitCallback)
        } else if (list.size == 0 || list == null) {
            listAudios = ArrayList()
            super.submitList(listAudios, commitCallback)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_mixer, parent, false)
        return RecentHolder(view)
    }


    override fun onBindViewHolder(holder: RecentHolder, position: Int) {
        holder.bind()
    }

    override fun onViewAttachedToWindow(holder: RecentHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: RecentHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(holder: RecentHolder, position: Int, payloads: MutableList<Any>) {
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
    fun updateItem(status: PlayerState, holder: RecentHolder, bitmap: Bitmap?) {
        when (status) {
            PlayerState.PLAYING -> {
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap).into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_play_default)
                }
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_play)
                holder.pgAudio.visibility = View.VISIBLE
                holder.waveView.visibility = View.VISIBLE

            }
            PlayerState.PAUSE -> {
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap).into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                }
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                holder.waveView.visibility = View.INVISIBLE
                holder.pgAudio.visibility = View.VISIBLE
            }
            PlayerState.IDLE -> {
                holder.pgAudio.visibility = View.GONE
                holder.waveView.visibility = View.INVISIBLE
                holder.pgAudio.resetView()

                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap).into(holder.ivController)
                } else {
                    holder.ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                }
                holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)

            }
            else -> {
                //nothing
            }
        }
    }

    inner class RecentHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, LifecycleOwner {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio_recent)
        val ivChecked = itemView.findViewById<ImageView>(R.id.iv_recent_check)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio_recent)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio_recent)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio_recent)
        val lnItem = itemView.findViewById<LinearLayout>(R.id.ln_item_audio_mixer_screen)
        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu_recent)
        val pgAudio = itemView.findViewById<ProgressView>(R.id.pg_audio_mixer_screen)
        val waveView = itemView.findViewById<WaveAudio>(R.id.wave_audio_mixer)
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
            itemAudioFile.state = playerState
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
            val itemAudioFile = getItem(position)
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
            updateItem(itemAudioFile.state, this, bitmap)

            when (itemAudioFile.isCheckChooseItem) {
                true -> ivChecked.setImageResource(R.drawable.ic_checkdone)
                false -> ivChecked.setImageResource(R.drawable.ic_noncheck)
            }
        }

        override fun onClick(p0: View) {
            if (adapterPosition == -1){
                return
            }
            when (p0.id) {
                R.id.iv_controller_audio_recent -> controllerAudio()
                R.id.ln_menu_recent -> {
                    checkItem()
                }
                R.id.ln_item_audio_mixer_screen -> {
                    checkItem()
                }
            }
        }

        private fun checkItem() {
            mCallBack.selectItem(adapterPosition)
        }

        private fun controllerAudio() {
            val itemAudio = listAudios.get(adapterPosition)
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
                }
                PlayerState.PAUSE -> {
                    audioPlayer.resume()
                }
                PlayerState.PLAYING -> {
                    audioPlayer.pause()
                }
                else -> {

                }
            }
        }
    }

    private fun checkValidGlide(bm: Bitmap?): Boolean {
        return (bm != null && !activity.isFinishing)
    }

    interface AudioMixerListener {
        fun selectItem(position:Int)
    }
}

class MixChooserAudioDiff : DiffUtil.ItemCallback<AudioCutterViewItem>() {
    override fun areItemsTheSame(
        oldItem: AudioCutterViewItem,
        newItem: AudioCutterViewItem
    ): Boolean {
        return oldItem.audioFile.getFilePath() == newItem.audioFile.getFilePath()
    }

    override fun areContentsTheSame(
        oldItem: AudioCutterViewItem,
        newItem: AudioCutterViewItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(
        oldItem: AudioCutterViewItem,
        newItem: AudioCutterViewItem
    ): Any? {
        val diff = Bundle()
        if (oldItem.isplaying != newItem.isplaying) {
            diff.putBoolean(ITEM_PLAY_BUTTON_CHANGED, true)
        }
        if (oldItem.currentPos != newItem.currentPos) {
            diff.putBoolean(ITEM_PROGRESSING_CHANGED, true)
        }
        if (oldItem.isCheckChooseItem != newItem.isCheckChooseItem) {
            diff.putBoolean(ITEM_CHECKBOX_CHANGED, true)
        }
        return diff
    }

}

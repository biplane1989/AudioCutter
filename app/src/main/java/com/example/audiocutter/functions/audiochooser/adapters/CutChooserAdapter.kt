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
import android.widget.PopupMenu
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.floor

class CutChooserAdapter(
    val mContext: Context,
    val audioPlayer: AudioPlayer,
    val lifecycleCoroutineScope: LifecycleCoroutineScope,
    val activity: Activity
) : ListAdapter<AudioCutterViewItem, CutChooserAdapter.AudiocutterHolder>(CutChooserAudioDiff()) {
    lateinit var mCallBack: CutChooserListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterViewItem>()

    fun setAudioCutterListtener(event: CutChooserListener) {
        mCallBack = event
    }

//    override fun submitList(list: List<AudioCutterView>?) {
//        if (list!!.size != 0 || list != null) {
//            listAudios = ArrayList(list)
//            super.submitList(listAudios)
//        } else if (list!!.size == 0 || list == null) {
//            listAudios = ArrayList()
//            super.submitList(listAudios)
//        }
//    }

    override fun submitList(list: MutableList<AudioCutterViewItem>?, commitCallback: Runnable?) {
        if (list!!.size != 0 || list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios, commitCallback)
        } else if (list.size == 0 || list == null) {
            listAudios = ArrayList()
            super.submitList(listAudios, commitCallback)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiocutterHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_cutter, parent, false)
        return AudiocutterHolder(view)
    }


    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int) {
        holder.bind()
    }

    override fun onViewAttachedToWindow(holder: AudiocutterHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: AudiocutterHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(
        holder: AudiocutterHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }else{
            val itemAudioFile = getItem(position)
            val diff = payloads.get(0) as Bundle

            if (diff.containsKey(ITEM_PROGRESSING_CHANGED)) {
                holder.pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration, true)
            }
            if (diff.containsKey(ITEM_PLAY_BUTTON_CHANGED)) {
                val bitmap = itemAudioFile.audioFile.bitmap
                updateItem(itemAudioFile.state, holder, bitmap)
            }
        }
    }

    fun updateItem(status: PlayerState, holder: AudiocutterHolder, bitmap: Bitmap?) {
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
                if (checkValidGlide(bitmap)) {
                    Glide.with(holder.itemView).load(bitmap).into(holder.ivController)
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


    inner class AudiocutterHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, LifecycleOwner {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)!!
        private val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio)
        private val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio)
        private val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio)
        val lnChild = itemView.findViewById<LinearLayout>(R.id.ln_item_audio_cutter_screen)
        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu)
        val pgAudio = itemView.findViewById<ProgressView>(R.id.pg_audio_cutter_screen)
        val waveView = itemView.findViewById<WaveAudio>(R.id.wave_audio_cutter)
        val ivPausePlay = itemView.findViewById<ImageView>(R.id.iv_pause_play_audio)

        init {
            ivController.setOnClickListener(this)
            lnMenu.setOnClickListener(this)
            lnChild.setOnClickListener(this)
        }


        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        var playerState: PlayerState = PlayerState.IDLE

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        private fun updatePlayInfo(playerInfo: PlayerInfo) {
            Log.d(
                "TAG",
                "updatePlayInfo: ${playerInfo.currentAudio?.fileName} - ${playerInfo.playerState}"
            )
            playerState = playerInfo.playerState
            val itemAudioFile = getItem(adapterPosition)
            val bitmap = itemAudioFile.audioFile.bitmap

            itemAudioFile.currentPos = playerInfo.posision.toLong()
            itemAudioFile.duration = playerInfo.duration.toLong()
            pgAudio.updatePG(playerInfo.posision.toLong(), playerInfo.duration.toLong())
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
                                updatePlayInfo(playerInfo)
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
            val itemAudioFile: AudioCutterViewItem = getItem(adapterPosition)
            var bitRate = itemAudioFile.audioFile.bitRate / 1000
            tvBitrateAudio.text = " | ${bitRate}${mContext.resources.getString(R.string.kbps)}"

            tvNameAudio.text = itemAudioFile.audioFile.fileName
            var size = (itemAudioFile.audioFile.size.toDouble() / SIZE_MB)

            if (size >= 1) {
                size = floor(size * 10) / 10
                tvSizeAudio.text = "$size ${mContext.resources.getString(R.string.megabyte)}"
            } else {
                size = (itemAudioFile.audioFile.size.toDouble() / 1024)
                size = floor(size * 10) / 10
                tvSizeAudio.text = "$size ${mContext.resources.getString(R.string.kilobyte)}"
            }

            if (itemAudioFile.currentPos > 0) {
                pgAudio.post {
                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration, false)
                }

            }
            val bitmap = itemAudioFile.audioFile.bitmap
            updateItem(itemAudioFile.state, this, bitmap)
        }

        override fun onClick(p0: View) {
            val itemAudio = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio -> controllerAudio()
                R.id.ln_item_audio_cutter_screen -> {
//                    controllerAudio()

                    mCallBack.onCutItemClicked(itemAudio)
                }
                R.id.ln_menu -> showPopupMenu(itemAudio)
            }
        }

        private fun controllerAudio() {
            val itemAudio = getItem(adapterPosition)
            mCallBack.onTickAudio(adapterPosition)
            if (adapterPosition == -1) {
                return
            }

            when (playerState) {
                PlayerState.IDLE -> {

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

        private fun showPopupMenu(itemAudio: AudioCutterViewItem) {
            val popupMenu = PopupMenu(mContext, lnMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_item_audio, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {
                    R.id.menu_cut -> {
                        mCallBack.onCutItemClicked(getItem(adapterPosition))
                    }
                    R.id.menu_play -> {
                        controllerAudio()
                    }
                    R.id.menu_setas -> {
                        mCallBack.showDialogSetAs(itemAudio)
                    }
                }
                false
            }
            popupMenu.show()

        }
    }

    private fun checkValidGlide(bitmap: Bitmap?): Boolean {
        return (bitmap != null && !activity.isFinishing)
    }

    interface CutChooserListener {
        fun showDialogSetAs(itemAudio: AudioCutterViewItem)
        fun onCutItemClicked(itemAudio: AudioCutterViewItem)
        fun onTickAudio(pos: Int)
    }
}

class CutChooserAudioDiff : DiffUtil.ItemCallback<AudioCutterViewItem>() {
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

    override fun getChangePayload(oldItem: AudioCutterViewItem, newItem: AudioCutterViewItem): Any {
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

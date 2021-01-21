package com.example.audiocutter.functions.audiochooser.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.event.OnItemTouchHelper
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.ui.audiochooser.cut.ProgressView
import com.example.audiocutter.ui.audiochooser.cut.WaveAudio
import com.example.audiocutter.ui.audiochooser.merge.MyItemTouchHelper
import kotlinx.coroutines.launch

const val ITEM_PLAY_BUTTON_CHANGED = "ITEM_PLAY_BUTTON_CHANGED"
const val ITEM_PROGRESSING_CHANGED = "ITEM_PROGRESSING_CHANGED"
const val ITEM_CHECKBOX_CHANGED = "ITEM_CHECKBOX_CHANGED"

class MergePreviewAdapter(
    val mContext: Context,
    val audioPlayer: AudioPlayer,
    val lifecycleCoroutineScope: LifecycleCoroutineScope,
    val activity: Activity,
    val mergerDiff: MergerChooserAudioDiff = MergerChooserAudioDiff()
) : ListAdapter<AudioCutterViewItem, MergePreviewAdapter.MergeChooseHolder>(mergerDiff),
    OnItemTouchHelper {

    //var listAudios = mutableListOf<AudioCutterView>()
    lateinit var mCallback: AudioMergeChooseListener
    private val mTouchHelper = MyItemTouchHelper(this, mContext)
    val itemTouchHelper = ItemTouchHelper(mTouchHelper)
    var playingStatus = PlayerState.IDLE
    var filePathPlaying = ""
    fun setOnCallBack(event: AudioMergeChooseListener) {
        mCallback = event
    }

    init {
        mergerDiff.myItemTouchHelper = mTouchHelper
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeChooseHolder {
        val itemView = LayoutInflater.from(mContext)
            .inflate(R.layout.item_audio_choose_merge, parent, false)
        return MergeChooseHolder(itemView)
    }

    override fun onBindViewHolder(holder: MergeChooseHolder, position: Int) {
        holder.bind()
    }

    override fun onViewAttachedToWindow(holder: MergeChooseHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: MergeChooseHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()

    }

    override fun onBindViewHolder(
        holder: MergeChooseHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val itemAudioFile = getItem(position)
            val bitmap = itemAudioFile.audioFile.bitmap

            if (TextUtils.equals(itemAudioFile.audioFile.getFilePath(), filePathPlaying)) {
                updateItem(playingStatus, holder, bitmap)
            } else {
                updateItem(itemAudioFile.state, holder, bitmap)
            }
        }
    }

    fun updateItem(status: PlayerState, holder: MergeChooseHolder, bitmap: Bitmap?) {
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


    private fun checkValidGlide(bitmap: Bitmap?): Boolean {
        return (bitmap != null && !activity.isFinishing)
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class MergeChooseHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener,
        LifecycleOwner {

        var ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)
        var tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_merge_choose_audio)
        var pgAudio = itemView.findViewById<ProgressView>(R.id.pg_merge_choose_screen)
        var waveView = itemView.findViewById<WaveAudio>(R.id.wave_merge_choose_cutter)
        var mGestureDetector: GestureDetector
        var ivTrash = itemView.findViewById<ImageView>(R.id.iv_trash_merge_choose)
        var ivMoveItem = itemView.findViewById<ImageView>(R.id.iv_move_item)
        val ivPausePlay = itemView.findViewById<ImageView>(R.id.iv_pause_play_audio)

        init {
            ivMoveItem.setOnTouchListener(this)
            ivTrash.setOnClickListener(this)
            mGestureDetector = GestureDetector(mContext, this)
            ivController.setOnClickListener(this)
        }

        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        var playerState: PlayerState = PlayerState.IDLE
        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        private fun updatePlayInfor(playerInfo: PlayerInfo) {
            playerState = playerInfo.playerState
            playingStatus = playerInfo.playerState
            pgAudio.updatePG(playerInfo.posision.toLong(), playerInfo.duration.toLong())

            val itemAudioFile = getItem(adapterPosition)
            val bitmap = itemAudioFile.audioFile.bitmap
            itemAudioFile.currentPos = playerInfo.posision.toLong()
            itemAudioFile.duration = playerInfo.duration.toLong()
            itemAudioFile.state = playerInfo.playerState

            filePathPlaying = playerInfo.currentAudio!!.getFilePath()
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
            Log.d("TAG", "updatePlayInfor: playerState:  resetItem" + playerState)
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


        fun bind() {
            val itemAudioFile = getItem(adapterPosition)
            if (audioPlayer.getPlayerInfoData().currentAudio?.getFilePath() == itemAudioFile.audioFile.getFilePath()) {
                playerState = audioPlayer.getPlayerInfoData().playerState
            }
            tvNameAudio.text = itemAudioFile.audioFile.fileName
            when (itemAudioFile.isCheckDistance) {
                true -> {
                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration)
                }
                false -> {
                    pgAudio.resetView()
                }
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
                        Glide.with(itemView).load(bitmap).into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_play_default)
                    }
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_play)
                    pgAudio.visibility = View.VISIBLE
                    waveView.visibility = View.VISIBLE

                }
                PlayerState.PAUSE -> {
                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap).into(ivController)
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
                        Glide.with(itemView).load(bitmap).into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                    }
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                }
                else -> {
                    //nothing
                }
            }


        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.iv_controller_audio -> controllerAudio()
                R.id.iv_trash_merge_choose -> deleteAudio()
            }
        }

        private fun deleteAudio() {
            try {
                mCallback.deleteAudio(getItem(adapterPosition))
                if (getItem(adapterPosition).audioFile.getFilePath().equals(filePathPlaying)) {
                    audioPlayer.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun controllerAudio() {
            val itemAudio = getItem(adapterPosition)
            if (adapterPosition == -1) {
                return
            }

            when (playerState) {
                PlayerState.IDLE -> {
//                    pgAudio.resetView()
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

        override fun onTouch(p0: View, motionEvent: MotionEvent): Boolean {

            Log.d("nqm", "onTouch: ${motionEvent.action}")

//            MergePreviewAdapter.statusTouch = true
            return mGestureDetector.onTouchEvent(motionEvent)
        }

        override fun onDown(p0: MotionEvent?): Boolean {
            itemTouchHelper.startDrag(this)
            return true
        }

        override fun onShowPress(motionEvent: MotionEvent?) {
            Log.d("nqm", "onShowPress: ")
        }

        override fun onSingleTapUp(motionEvent: MotionEvent?): Boolean {
            Log.d("nqm", "onSingleTapUp: ")
            return false
        }

        override fun onScroll(
            motionEvent: MotionEvent?,
            p1: MotionEvent?,
            p2: Float,
            p3: Float
        ): Boolean {
            Log.d("nqm", "onScroll: ")
            return false
        }


        override fun onLongPress(p0: MotionEvent?) {
            /*           mTouchHelper.startDrag(this)*/
            Log.d("nqm", "onLongPress: on longpress")
        }

        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            Log.d("nqm", "onFling: ")
            return false
        }
    }

    interface AudioMergeChooseListener {
        fun deleteAudio(fileName: AudioCutterViewItem)
        fun moveItemAudio(prePos: Int, nextPos: Int)
    }

    override fun moveItem(prePos: Int, nextPos: Int) {
        mCallback.moveItemAudio(prePos, nextPos)
        notifyItemMoved(prePos, nextPos)
    }
}

class MergerChooserAudioDiff() : DiffUtil.ItemCallback<AudioCutterViewItem>() {
    var myItemTouchHelper: MyItemTouchHelper? = null
    override fun areItemsTheSame(
        oldItem: AudioCutterViewItem,
        newItem: AudioCutterViewItem
    ): Boolean {
        myItemTouchHelper?.let {
            if (it.isDragging()) {
                return true
            }
        }
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

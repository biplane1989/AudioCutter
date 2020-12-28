package com.example.audiocutter.functions.audiochooser.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
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
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.ui.audiochooser.cut.ProgressView
import com.example.audiocutter.ui.audiochooser.cut.WaveAudio
import kotlinx.coroutines.launch

class MergePreviewAdapter(
    val mContext: Context,
    val audioPlayer: AudioPlayer,
    val lifecycleCoroutineScope: LifecycleCoroutineScope,
    val activity: Activity
) : ListAdapter<AudioCutterView, MergePreviewAdapter.MergeChooseHolder>(MergerChooserAudioDiff()),
    OnItemTouchHelper {

    //var listAudios = mutableListOf<AudioCutterView>()
    lateinit var mCallback: AudioMergeChooseListener
    lateinit var mTouchHelper: ItemTouchHelper

    fun setOnCallBack(event: AudioMergeChooseListener) {
        mCallback = event
    }

    override fun submitList(list: List<AudioCutterView>?) {

        if (list != null) {
            super.submitList(ArrayList(list))
        } else {
            super.submitList(ArrayList())
        }
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

    override fun onBindViewHolder(holder: MergeChooseHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val itemAudioFile = getItem(position)


            val bitmap = itemAudioFile.audioFile.bitmap
            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    if (checkValidGlide(bitmap)) {
                        Glide.with(holder.itemView).load(bitmap)
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

                            .into(holder.ivController)
                    } else {
                        holder.ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                    }
                    holder.ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                    holder.waveView.visibility = View.INVISIBLE
                }
                PlayerState.IDLE -> {
                    holder.pgAudio.visibility = View.GONE
                    holder.waveView.visibility = View.INVISIBLE
                    holder.pgAudio.resetView()

                    if (checkValidGlide(bitmap)) {
                        Glide.with(holder.itemView).load(bitmap)
                            .into(holder.ivController)
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
            pgAudio.updatePG(playerInfo.posision.toLong(), playerInfo.duration.toLong())
            Log.d("giangtd123", "updatePlayInfor: pecent: " + playerInfo.posision.toLong() + "status: " + playerInfo.playerState)

            val itemAudioFile = getItem(adapterPosition)
            val bitmap = itemAudioFile.audioFile.bitmap
            itemAudioFile.currentPos = playerInfo.posision.toLong()
            itemAudioFile.duration = playerInfo.duration.toLong()
            when (playerInfo.playerState) {
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
                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap)
//                            .transform(RoundedCorners(Utils.convertDp2Px(12, itemView.context)
//                                .toInt()))
                            .into(ivController)
                    } else {
                        ivController.setImageResource(R.drawable.common_audio_item_bg_pause_default)
                    }
                    pgAudio.visibility = View.GONE
                    waveView.visibility = View.INVISIBLE
                    pgAudio.resetView()
                    ivPausePlay.setImageResource(R.drawable.common_audio_item_pause)
                }
                else -> {
                    //nothing
                }
            }
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
                            }else {
                                resetItem(audioCutterView)
                            }
                        }
                    }
                }
            })
        }

        fun resetItem(audioCutterView: AudioCutterView) {

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


        fun bind() {
            val itemAudioFile = getItem(adapterPosition)

            tvNameAudio.text = itemAudioFile.audioFile.fileName
            when (itemAudioFile.isCheckDistance) {
                true -> {
                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration)
                }
                false -> {
                    pgAudio.resetView()
                }
            }
            if(itemAudioFile.currentPos>0){
                pgAudio.post {
                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration, false)
                }

            }
            val bitmap = itemAudioFile.audioFile.bitmap

            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    if (checkValidGlide(bitmap)) {
                        Glide.with(itemView).load(bitmap)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun controllerAudio() {
            val itemAudio = getItem(adapterPosition)
            if (adapterPosition == -1) {
                return
            }
//            when (itemAudio.state) {
//                PlayerState.IDLE -> {
//                    mCallback.play(adapterPosition)
//                }
//                PlayerState.PAUSE -> {
//                    mCallback.resume(adapterPosition)
//                }
//                PlayerState.PLAYING -> {
//                    mCallback.pause(adapterPosition)
//                }
//            }

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

        override fun onTouch(p0: View?, motionEvent: MotionEvent?): Boolean {
            mGestureDetector.onTouchEvent(motionEvent)
            Log.d("nqm", "onTouch: ")
            return false
        }

        override fun onDown(p0: MotionEvent?): Boolean {
            mTouchHelper.startDrag(this)
            Log.d("nqm", "onDown: ")
            return true
        }

        override fun onShowPress(motionEvent: MotionEvent?) {
        }

        override fun onSingleTapUp(motionEvent: MotionEvent?): Boolean {
            Log.d("nqm", "onSingleTapUp: ")
            return false
        }

        override fun onScroll(motionEvent: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
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
        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun deleteAudio(fileName: AudioCutterView)
        fun moveItemAudio(prePos: Int, nextPos: Int)
    }

    override fun moveItem(prePos: Int, nextPos: Int) {
        mCallback.moveItemAudio(prePos, nextPos)
        notifyItemMoved(prePos, nextPos)
    }


    fun setTouchHelper(touchHelper: ItemTouchHelper) {
        this.mTouchHelper = touchHelper
    }
}

class MergerChooserAudioDiff : DiffUtil.ItemCallback<AudioCutterView>() {
    override fun areItemsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem.audioFile.fileName == oldItem.audioFile.fileName
    }

    override fun areContentsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: AudioCutterView, newItem: AudioCutterView): Any? {
        return newItem.isCheckChooseItem
    }

}

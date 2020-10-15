package com.example.audiocutter.functions.audiochooser.adapters

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.ui.audiochooser.cut.ProgressView
import com.example.audiocutter.ui.audiochooser.cut.WaveAudio
import com.example.audiocutter.functions.audiochooser.event.OnItemTouchHelper

class MergePreviewAdapter(val mContext: Context) :
    ListAdapter<AudioCutterView, MergePreviewAdapter.MergeChooseHolder>(
        MergerChooserAudioDiff()
    ), OnItemTouchHelper {

    var listAudios = mutableListOf<AudioCutterView>()
    lateinit var mCallback: AudioMergeChooseListener
    lateinit var mTouchHelper: ItemTouchHelper

    fun setOnCallBack(event: AudioMergeChooseListener) {
        mCallback = event
    }

    override fun submitList(list: List<AudioCutterView>?) {

        if (list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios)
        } else {
            listAudios = ArrayList()
            super.submitList(listAudios)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeChooseHolder {
        val itemView =
            LayoutInflater.from(mContext).inflate(R.layout.item_audio_choose_merge, parent, false)
        return MergeChooseHolder(itemView)
    }

    override fun onBindViewHolder(holder: MergeChooseHolder, position: Int) {
        holder.bind()
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

            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    holder.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                }
                PlayerState.PAUSE -> {
                    holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                }
                PlayerState.IDLE -> {
                    holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                }
            }
        }
    }

    inner class MergeChooseHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {

        var ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)
        var tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_merge_choose_audio)
        var pgAudio = itemView.findViewById<ProgressView>(R.id.pg_merge_choose_screen)
        var waveView = itemView.findViewById<WaveAudio>(R.id.wave_merge_choose_cutter)
        var mGestureDetector: GestureDetector
        var ivTrash = itemView.findViewById<ImageView>(R.id.iv_trash_merge_choose)
        var ivMoveItem = itemView.findViewById<ImageView>(R.id.iv_move_item)

        init {
            ivMoveItem.setOnTouchListener(this)
            ivTrash.setOnClickListener(this)
            mGestureDetector = GestureDetector(mContext, this)
            ivController.setOnClickListener(this)
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

            when (itemAudioFile.state) {

                PlayerState.PLAYING -> {
                    pgAudio.visibility = View.VISIBLE
                    waveView.visibility = View.VISIBLE
                    ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                }
                PlayerState.PAUSE -> {
                    waveView.visibility = View.INVISIBLE
                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
                }
                PlayerState.IDLE -> {
                    pgAudio.visibility = View.GONE
                    waveView.visibility = View.INVISIBLE
                    pgAudio.resetView()
                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
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
            mCallback.deleteAudio(adapterPosition)
        }

        private fun controllerAudio() {
            val itemAudio = listAudios[adapterPosition]
            if (adapterPosition == -1) {
                return
            }
            when (itemAudio.state) {
                PlayerState.IDLE -> {
                    mCallback.play(adapterPosition)
                }
                PlayerState.PAUSE -> {
                    mCallback.resume(adapterPosition)
                }
                PlayerState.PLAYING -> {
                    mCallback.pause(adapterPosition)
                }
            }
        }

        override fun onTouch(p0: View?, motionEvent: MotionEvent?): Boolean {
            mGestureDetector.onTouchEvent(motionEvent)
            return false
        }

        override fun onDown(p0: MotionEvent?): Boolean {
            mTouchHelper.startDrag(this)
            return true
        }

        override fun onShowPress(p0: MotionEvent?) {
        }

        override fun onSingleTapUp(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }


        override fun onLongPress(p0: MotionEvent?) {
            Log.d("TAG", "onLongPress: on longpress")

        }

        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }
    }


    interface AudioMergeChooseListener {
        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun deleteAudio(pos: Int)
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
        return newItem.state
    }

}

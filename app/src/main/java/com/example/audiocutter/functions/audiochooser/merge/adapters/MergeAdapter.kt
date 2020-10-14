package com.example.audiocutter.functions.audiochooser.merge.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.cut.objs.AudioCutterView
import com.example.audiocutter.functions.audiochooser.cut.widget.ProgressView
import com.example.audiocutter.functions.audiochooser.cut.widget.WaveAudio

class MergeAdapter(val mContext: Context) : ListAdapter<AudioCutterView, MergeAdapter.MergeHolder>(
    MergerDiff()
) {
    lateinit var mCallBack: AudioMergeListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterView>()


    fun setAudioListener(event: AudioMergeListener) {
        mCallBack = event
    }

    override fun submitList(list: List<AudioCutterView>?) {
        if (list!!.size != 0 || list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios)
        } else
            if (list!!.size == 0 || list == null) {
                listAudios = ArrayList()
                super.submitList(listAudios)
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_merger, parent, false)
        return MergeHolder(view)
    }


    override fun onBindViewHolder(holder: MergeHolder, position: Int) {
        holder.bind()

    }


    override fun onBindViewHolder(holder: MergeHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val itemAudioFile = getItem(position)
//            val audioCutterView = payloads.firstOrNull() as AudioCutterView


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
            when (itemAudioFile.isCheckChooseItem) {
                true -> holder.ivChecked.setImageResource(R.drawable.ic_checkdone)
                false -> holder.ivChecked.setImageResource(R.drawable.ic_noncheck)
            }
        }
    }

    inner class MergeHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio_merger)
        val ivChecked = itemView.findViewById<ImageView>(R.id.iv_merger_check)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio_merger)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio_merger)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio_merger)
        val lnItem = itemView.findViewById<LinearLayout>(R.id.ln_item_audio_merger_screen)

        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu_merger)

        val pgAudio = itemView.findViewById<ProgressView>(R.id.pg_audio_merger_screen)
        val waveView = itemView.findViewById<WaveAudio>(R.id.wave_audio_merger)

        init {
            ivController.setOnClickListener(this)
            lnMenu.setOnClickListener(this)
            lnItem.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val itemAudioFile = getItem(adapterPosition)
            tvBitrateAudio.text = "${itemAudioFile.audioFile.bitRate}Kbs/s"

            tvNameAudio.text = itemAudioFile.audioFile.fileName
            var size = (itemAudioFile.audioFile.size.toDouble() / SIZE_MB)

            if (size >= 1) {
                size = Math.floor(size * 10) / 10
                tvSizeAudio.text = "$size Mb"
            } else {
                size = (itemAudioFile.audioFile.size.toDouble() / 1024)
                size = Math.floor(size * 10) / 10
                tvSizeAudio.text = "$size Kb"
            }

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

            when (itemAudioFile.isCheckChooseItem) {
                true -> ivChecked.setImageResource(R.drawable.ic_checkdone)
                false -> ivChecked.setImageResource(R.drawable.ic_noncheck)
            }

        }


        override fun onClick(p0: View) {
            val item = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio_merger -> controllerAudio()
                R.id.ln_menu_merger -> mCallBack.chooseItemAudio(adapterPosition, item.isCheckChooseItem)
                R.id.ln_item_audio_merger_screen -> mCallBack.chooseItemAudio(adapterPosition, item.isCheckChooseItem)
            }
        }


        private fun controllerAudio() {
            val itemAudio = listAudios[adapterPosition]
            if (adapterPosition == -1) {
                return
            }
            when (itemAudio.state) {
                PlayerState.IDLE -> {
                    mCallBack.play(adapterPosition)
                }
                PlayerState.PAUSE -> {
                    mCallBack.resume(adapterPosition)
                }
                PlayerState.PLAYING -> {
                    mCallBack.pause(adapterPosition)
                }
            }
        }


    }

    interface AudioMergeListener {

        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun chooseItemAudio(pos: Int, rs: Boolean)
    }
}



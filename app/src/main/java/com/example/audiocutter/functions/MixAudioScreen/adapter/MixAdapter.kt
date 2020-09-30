package com.example.audiocutter.functions.MixAudioScreen.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView

class MixAdapter(val mContext: Context) :
    ListAdapter<AudioCutterView, MixAdapter.RecentHolder>(Audiodiff()) {
    lateinit var mCallBack: AudioMixerListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterView>()


    fun setAudioCutterListtener(event: AudioMixerListener) {
        mCallBack = event
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_mixer, parent, false)
        return RecentHolder(view)
    }


    override fun onBindViewHolder(holder: RecentHolder, position: Int) {
        holder.bind()

    }


    override fun onBindViewHolder(
        holder: RecentHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
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
                true -> holder.ivChecked.setImageResource(R.drawable.ic_mixer_checkdone)
                false -> holder.ivChecked.setImageResource(R.drawable.ic_mixer_noncheck)
            }
        }
    }

    inner class RecentHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio_recent)
        val ivChecked = itemView.findViewById<ImageView>(R.id.iv_recent_check)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio_recent)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio_recent)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio_recent)
        val lnItem = itemView.findViewById<LinearLayout>(R.id.ln_item_audio_recent_screen)

        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu_recent)

        init {
            ivController.setOnClickListener(this)
            lnMenu.setOnClickListener(this)
            lnItem.setOnClickListener(this)
        }

        fun bind() {
            val itemAudioFile = getItem(position)
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

            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                    return
                }
                PlayerState.PAUSE -> {
                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    return
                }
                PlayerState.IDLE -> {
                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
                }
            }
            when (itemAudioFile.isCheckChooseItem) {
                true -> ivChecked.setImageResource(R.drawable.ic_mixer_checkdone)
                false -> ivChecked.setImageResource(R.drawable.ic_mixer_noncheck)
            }

        }


        override fun onClick(p0: View) {
            val item = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio_recent -> controllerAudio()
                R.id.ln_menu_recent -> mCallBack.chooseItemAudio(adapterPosition, item.isCheckChooseItem)
                R.id.ln_item_audio_recent_screen -> mCallBack.chooseItemAudio(adapterPosition, item.isCheckChooseItem)
            }
        }



        private fun controllerAudio() {
            val itemAudio = listAudios.get(adapterPosition)
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

    interface AudioMixerListener {

        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun chooseItemAudio(pos: Int, rs: Boolean)
    }
}

class Audiodiff : DiffUtil.ItemCallback<AudioCutterView>() {
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

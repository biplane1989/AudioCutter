package com.example.audiocutter.functions.audiocutterscreen.view.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView

class AudiocutterAdapter(val mContext: Context) :
    ListAdapter<AudioCutterView, AudiocutterAdapter.AudiocutterHolder>(Audiodiff()) {
    lateinit var mCallBack: AudioCutterListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterView>()
    var currentHolder: AudiocutterHolder? = null



    fun setAudioCutterListtener(event: AudioCutterListener) {
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiocutterHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false)
        return AudiocutterHolder(view)
    }


    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int) {
        val itemAudioFile = getItem(position)
        holder.tvBitrateAudio.text = "${itemAudioFile.audioFile.bitRate}Kbs/s"

        holder.tvNameAudio.text = itemAudioFile.audioFile.fileName
        var size = (itemAudioFile.audioFile.size.toDouble() / SIZE_MB)

        if (size >= 1) {
            size = Math.floor(size * 10) / 10
            holder.tvSizeAudio.text = "$size Mb"
        } else {
            size = (itemAudioFile.audioFile.size.toDouble() / 1024)
            size = Math.floor(size * 10) / 10
            holder.tvSizeAudio.text = "$size Kb"
        }
        when (itemAudioFile.state ) {
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


    override fun onBindViewHolder(
        holder: AudiocutterHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }
    }

    inner class AudiocutterHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio)
        val lnMenu = itemView.findViewById<LinearLayout>(R.id.ln_menu)

        init {
            ivController.setOnClickListener(this)
            lnMenu.setOnClickListener(this)
        }


        override fun onClick(p0: View) {
            val itemAudio = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio -> controllerAudio()
                R.id.ln_menu -> showPopupMenu(itemAudio)
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


        private fun showPopupMenu(itemAudio: AudioCutterView) {
            val popupMenu = PopupMenu(mContext, lnMenu)

            popupMenu.menuInflater.inflate(R.menu.menu_item_audio, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {
                    R.id.menu_cut -> {
                        Toast.makeText(mContext, "cut", Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu_play
                    -> {
                        controllerAudio()
                    }
                    R.id.menu_contacts
                    -> {
                        Toast.makeText(mContext, "contacts", Toast.LENGTH_SHORT).show()
                    }
                    R.id.menu_setas
                    -> {
                        mCallBack.showDialogSetAs(itemAudio)
                    }
                }
                false
            }
            popupMenu.show()

        }
    }

    interface AudioCutterListener {

        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun showDialogSetAs(itemAudio: AudioCutterView) {
        }
    }
}

class Audiodiff : DiffUtil.ItemCallback<AudioCutterView>() {
    override fun areItemsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem.audioFile.fileName == oldItem.audioFile.fileName
    }

    override fun areContentsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == newItem
    }

}

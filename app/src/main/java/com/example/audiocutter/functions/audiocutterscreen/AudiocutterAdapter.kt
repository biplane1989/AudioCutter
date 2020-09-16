package com.example.audiocutter.functions.audiocutterscreen


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.objects.AudioFile

class AudiocutterAdapter(val mContext: Context) :
    ListAdapter<AudioCutterView, AudiocutterAdapter.AudiocutterHolder>(Audiodiff()) {
    lateinit var mCallBack: AudioCutterListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterView>()
    var playerInfo: PlayerInfo? = null
    var currentHolder: AudiocutterHolder? = null
    var currentItemIndex = -1


    fun setAudioCutterListtener(event: AudioCutterListener) {
        mCallBack = event
    }

    override fun submitList(list: List<AudioCutterView>?) {
        if (list == null) {
            listAudios.clear()
        } else {
            listAudios.clear()
            listAudios.addAll(list)
        }
        super.submitList(list)
        currentHolder = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiocutterHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false)
        return AudiocutterHolder(view)
    }

    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int) {
        val itemAudioFile = getItem(position)

        holder.tvBitrateAudio.text = "${itemAudioFile.audioFile.bitRate}Kbs/s"
        holder.tvNameAudio.text = itemAudioFile.audioFile.fileName

        var size = (itemAudioFile.audioFile.size / SIZE_MB).toDouble()

        if (size >= 1) {

            holder.tvSizeAudio.text = "$size Mb"
        } else {
            size = (itemAudioFile.audioFile.size / 1024).toString().toDouble()
            holder.tvSizeAudio.text = "$size Kb"
        }


        playerInfo?.let {
            if (it.playerState != PlayerState.IDLE && it.currentAudio == itemAudioFile.audioFile) {
                currentHolder = holder
            }
        }
        holder.ivController.tag = itemAudioFile


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
            val itemAudio = ivController.tag as AudioCutterView
            when (p0.id) {
                R.id.iv_controller_audio -> {
                    controllerAudio()
                }
                R.id.ln_menu -> {
                    showPopupMenu(itemAudio)
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


        private fun controllerAudio() {
            ivController.setImageResource(R.drawable.ic_audiocutter_pause)
            val oldHolder = currentHolder
            currentHolder = this
            val itemAudioCutter = listAudios.get(adapterPosition)

            when (itemAudioCutter.state) {
                PlayerState.IDLE -> {
                    if (oldHolder != currentHolder && oldHolder != null) {
                        oldHolder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    }
//                    ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                    mCallBack.play(itemAudioCutter.audioFile)
                }
                PlayerState.PAUSE -> {
//                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    mCallBack.resume()
                }
                PlayerState.PLAYING -> {
//                    ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                    mCallBack.pause()
                }
            }
        }
    }

    fun mediaInfoUpdate(playerInfo: PlayerInfo) {
        Log.d(
            "taih",
            "${playerInfo.currentAudio?.file?.path ?: "null"} -> ${playerInfo.playerState}"
        )

        val runningPos = getRunningAudioPos(playerInfo)

        if (runningPos != -1) {
            val audioFileView = listAudios.get(runningPos)
            audioFileView.state = playerInfo.playerState
            if (currentHolder != null) {
                if (audioFileView.state == PlayerState.PLAYING) {
                    currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                } else {
                    currentHolder!!.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                }
            }
        }
    }



    private fun getRunningAudioPos(playerInfo: PlayerInfo): Int {
        var runningPos = -1
        var index = 0
        while (index < listAudios.size) {
            if (listAudios.get(index).audioFile == playerInfo.currentAudio) {
                runningPos = index
                break
            }
            index++
        }
        return runningPos
    }


    interface AudioCutterListener {

        fun play(audioFile: AudioFile)
        fun pause()
        fun resume()
        fun stop()
        fun showDialogSetAs(itemAudio: AudioCutterView) {
        }
    }
}

class Audiodiff : DiffUtil.ItemCallback<AudioCutterView>() {
    override fun areItemsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == oldItem
    }

    override fun areContentsTheSame(oldItem: AudioCutterView, newItem: AudioCutterView): Boolean {
        return oldItem == newItem
    }

}

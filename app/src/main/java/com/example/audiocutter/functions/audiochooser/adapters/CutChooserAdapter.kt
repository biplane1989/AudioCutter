package com.example.audiocutter.functions.audiochooser.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.audiocutter.R
import com.example.audiocutter.core.audiomanager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.ui.audiochooser.cut.ProgressView
import com.example.audiocutter.ui.audiochooser.cut.WaveAudio
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import kotlinx.coroutines.launch
import kotlin.math.floor

class CutChooserAdapter(val mContext: Context, val audioPlayer: AudioPlayer, val lifecycleCoroutineScope: LifecycleCoroutineScope) : ListAdapter<AudioCutterView, CutChooserAdapter.AudiocutterHolder>(CutChooserAudioDiff()) {
    lateinit var mCallBack: CutChooserListener
    val SIZE_MB = 1024 * 1024
    var listAudios = mutableListOf<AudioCutterView>()

    fun setAudioCutterListtener(event: CutChooserListener) {
        mCallBack = event
    }

    override fun submitList(list: List<AudioCutterView>?) {
        if (list!!.size != 0 || list != null) {
            listAudios = ArrayList(list)
            super.submitList(listAudios)
        } else if (list!!.size == 0 || list == null) {
            listAudios = ArrayList()
            super.submitList(listAudios)
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

    override fun onBindViewHolder(holder: AudiocutterHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
//            val itemAudioFile = getItem(position)
//            val audioCutterView = payloads.firstOrNull() as AudioCutterView

            /*   when (audioCutterView.state) {
                   PlayerState.PLAYING -> {
                       holder.ivController.setImageResource(R.drawable.ic_audiocutter_pause)
                   }
                   PlayerState.PAUSE -> {
                       holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                   }
                   PlayerState.IDLE -> {
                       holder.pgAudio.resetView()
   //                    holder.ivController.setImageResource(R.drawable.ic_audiocutter_play)
                       val bitmap = itemAudioFile.audioFile.bitmap
                       if (bitmap != null) {
                           Glide.with(holder.itemView).load(bitmap)
                               .transform(
                                   RoundedCorners(
                                       Utils.convertDp2Px(4, context = holder.itemView.context).toInt()
                                   )
                               )
                               .into( holder.ivController)
   //                        holder.ivController.setImageBitmap(bitmap)
                       } else {
                           val bm = BitmapFactory.decodeResource(
                               AudioFileManagerImpl.mContext.resources,
                               R.drawable.ic_audiocutter_play
                           )
                           holder.ivController.setImageBitmap(bm)
                       }
                   }
               }*/

        }
    }

    inner class AudiocutterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LifecycleOwner {

        val ivController = itemView.findViewById<ImageView>(R.id.iv_controller_audio)!!
        val tvNameAudio = itemView.findViewById<TextView>(R.id.tv_name_audio)
        val tvSizeAudio = itemView.findViewById<TextView>(R.id.tv_size_audio)
        val tvBitrateAudio = itemView.findViewById<TextView>(R.id.tv_bitrate_audio)
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

        private fun updatePlayInfor(playerInfo: PlayerInfo) {
            playerState = playerInfo.playerState
            pgAudio.updatePG(playerInfo.posision.toLong(), playerInfo.duration.toLong())
            Log.d("giangtd123", "updatePlayInfor: pecent: " + playerInfo.posision + " duration : " + playerInfo.duration + "status: " + playerInfo.playerState)
            val itemAudioFile = getItem(adapterPosition)
            val bitmap = itemAudioFile.audioFile.bitmap

            when (playerInfo.playerState) {
                PlayerState.PLAYING -> {
                    if (bitmap != null) {
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
                    if (bitmap != null) {
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
                    if (bitmap != null) {
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
                            }
                        }
                    }
                }
            })
        }

        fun onViewDetachedFromWindow() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val itemAudioFile = getItem(adapterPosition)
            Log.d("TAG", "bind: ${itemAudioFile.isCheckDistance}    state ${itemAudioFile.state}")
            var bitRate = itemAudioFile.audioFile.bitRate / 1000

            tvBitrateAudio.text = "${bitRate}${mContext.resources.getString(R.string.kbps)}"

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

//            when (itemAudioFile.isCheckDistance) {
//                true -> {
//                    pgAudio.updatePG(itemAudioFile.currentPos, itemAudioFile.duration)
//                }
//                false -> {
//                    pgAudio.resetView()
//                }
//            }

            /*when (itemAudioFile.state) {

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
//                    ivController.setImageResource(R.drawable.ic_audiocutter_play)
                    val bitmap =   itemAudioFile.audioFile.bitmap
                    if(bitmap!=null){
                        Glide.with(itemView).load(bitmap)
                            .transform(
                                RoundedCorners(
                                    Utils.convertDp2Px(4, itemView.context).toInt()
                                )
                            )
                            .into(ivController)
                    }else{
                        val bm = BitmapFactory.decodeResource(
                            AudioFileManagerImpl.mContext.resources,
                            R.drawable.ic_audiocutter_play
                        )
                       ivController.setImageBitmap(bm)
                    }
                }
            }*/

            val bitmap = itemAudioFile.audioFile.bitmap

            when (itemAudioFile.state) {
                PlayerState.PLAYING -> {
                    if (bitmap != null) {
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
                    if (bitmap != null) {
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
                    pgAudio.resetView()

                    if (bitmap != null) {
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
        }

        override fun onClick(p0: View) {
            val itemAudio = getItem(adapterPosition)
            when (p0.id) {
                R.id.iv_controller_audio -> controllerAudio()
                R.id.ln_item_audio_cutter_screen -> mCallBack.onCutItemClicked(itemAudio)
                R.id.ln_menu -> showPopupMenu(itemAudio)
            }
        }

        private fun controllerAudio() {
            val itemAudio = getItem(adapterPosition)
            if (adapterPosition == -1) {
                return
            }
            when (playerState) {
                PlayerState.IDLE -> {
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

        private fun showPopupMenu(itemAudio: AudioCutterView) {
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
                    /*  R.id.menu_contacts -> {

                          mCallBack.setRingtoneContact(itemAudio.audioFile.file.absolutePath)
  //                        Toast.makeText(mContext, "contacts", Toast.LENGTH_SHORT).show()
                      }*/
                    R.id.menu_setas -> {
                        mCallBack.showDialogSetAs(itemAudio)
                    }
                }
                false
            }
            popupMenu.show()

        }
    }

    interface CutChooserListener {
        fun play(pos: Int)
        fun pause(pos: Int)
        fun resume(pos: Int)
        fun showDialogSetAs(itemAudio: AudioCutterView)
        fun onCutItemClicked(itemAudio: AudioCutterView)
//        fun setRingtoneContact(filePath: String)
    }
}

class CutChooserAudioDiff : DiffUtil.ItemCallback<AudioCutterView>() {
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

package com.example.audiocutter.functions.mystudio.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.util.Util
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.*
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

interface AudioCutterScreenCallback {
    fun showMenu(view: View, audioFile: AudioFile)
    fun checkDeletePos(position: Int)
    fun isShowPlayingAudio(position: Int)
    fun cancelLoading(id: Int)
    fun errorConverting(fileName: String)
}

class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback, val audioPlayer: AudioPlayer, val audioEditorManager: AudioEditorManager, val lifecycleCoroutineScope: LifecycleCoroutineScope) : ListAdapter<AudioFileView, AudioCutterAdapter.MyStudioHolder>(MusicDiffCallBack()) {

    private val TAG = "giangtd"

    @SuppressLint("SimpleDateFormat")
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    private var mListAudio = ArrayList<AudioFileView>()
    private lateinit var recyclerView: RecyclerView


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStudioHolder {
        recyclerView = parent as RecyclerView
        return if (viewType == SUCCESS_VIEW) {
            SuccessViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.my_studio_screen_item, parent, false))
        } else LoadingViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_item_loading, parent, false))


    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).convertingState == ConvertingState.SUCCESS) {
            SUCCESS_VIEW
        } else {
            LOADING_VIEW
        }
    }

    override fun onViewAttachedToWindow(holder: MyStudioHolder) {       // khi view dc hien thi tren man hinh
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()

    }

    override fun onViewDetachedFromWindow(holder: MyStudioHolder) {     // khi view bi destroy tren man hinh
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()

    }

    override fun submitList(list: List<AudioFileView>?) {
        if (list != null) {
            mListAudio = ArrayList(list)
            super.submitList(ArrayList(list))
        } else {
            super.submitList(null)
        }

    }

    override fun onBindViewHolder(holder: MyStudioHolder, position: Int) {
        if (SUCCESS_VIEW == getItemViewType(position)) {
            val successViewHolder = holder as SuccessViewHolder
            successViewHolder.onBind()
        } else {
            val loadingViewHolder = holder as LoadingViewHolder
            loadingViewHolder.onBind()
        }
    }


    // khi chi thay doi 1 truong trong data
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyStudioHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (holder is SuccessViewHolder) {
                val successViewHolder = holder as SuccessViewHolder
                val newItem = payloads.firstOrNull() as AudioFileView

                when (newItem.itemLoadStatus.deleteState) {
                    DeleteState.HIDE -> {
                        successViewHolder.ivItemDelete.visibility = View.GONE
                        successViewHolder.ivSetting.visibility = View.VISIBLE
                    }
                    DeleteState.UNCHECK -> {
                        successViewHolder.ivSetting.visibility = View.INVISIBLE
                        successViewHolder.ivItemDelete.visibility = View.VISIBLE
                        successViewHolder.ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
                    }
                    DeleteState.CHECKED -> {
                        successViewHolder.ivSetting.visibility = View.INVISIBLE
                        successViewHolder.ivItemDelete.visibility = View.VISIBLE
                        successViewHolder.ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_checked)
                    }
                }

                Log.d(TAG, "onBindViewHolder: " + newItem.isExpanded)

                if (newItem.isExpanded) {
                    successViewHolder.llPlayMusic.visibility = View.VISIBLE
                    successViewHolder.llItem.setBackgroundResource(R.drawable.my_studio_item_bg)

                    Log.d(TAG, "onBindViewHolder: 1")
                    holder.itemView.post {
                        if (holder.itemView.bottom > recyclerView.height) {
                            recyclerView.smoothScrollBy(0, (holder.itemView.bottom - recyclerView.height))
                        }
                    }

                } else {
                    successViewHolder.llPlayMusic.visibility = View.GONE
                    successViewHolder.llItem.setBackgroundColor(Color.WHITE)
                    successViewHolder.llItem.setPadding(0, 0, 0, 0)
                }

            } else {
                val loadingViewHolder = holder as LoadingViewHolder
                val newItem = payloads.firstOrNull() as AudioFileView
                val convertingItem = getItem(position)

                Log.d(TAG, "onBindViewHolder: convertingItem.percent : " + convertingItem.percent)

                when (newItem.convertingState) {
                    ConvertingState.WAITING -> {
                        loadingViewHolder.tvWait.visibility = View.VISIBLE
                        loadingViewHolder.pbLoading.visibility = View.GONE
                        loadingViewHolder.tvLoading.visibility = View.GONE
                    }
                    ConvertingState.PROGRESSING -> {
                        loadingViewHolder.tvWait.visibility = View.GONE
                        loadingViewHolder.pbLoading.visibility = View.VISIBLE
                        loadingViewHolder.tvLoading.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    inner abstract class MyStudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LifecycleOwner {
        private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)      // tao 1 lifecycleRegistry

        override fun getLifecycle(): Lifecycle {                                                // gan lifecycleRegistry tu dinh nghia cho class
            return lifecycleRegistry
        }

        open fun onViewAttachedToWindow() {                                                // gan view vao trong windows do minh tu viet
            lifecycleRegistry.currentState = Lifecycle.State.STARTED                  // livedata o trang thai is Active thi moi hoat dong STARTED or RESUMED
        }

        open fun onViewDetachedFromWindow() {                                                // go~ view ra khoi windows do minh tu viet
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }

    inner class SuccessViewHolder(itemView: View) : MyStudioHolder(itemView), View.OnClickListener {
        val sbMusic: SeekBar = itemView.findViewById(R.id.sb_music)
        val tvTimeLife: TextView = itemView.findViewById(R.id.tv_time_life)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val ivSetting: ImageView = itemView.findViewById(R.id.iv_setting)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvInfo: TextView = itemView.findViewById(R.id.tv_info_music)

        val llPlayMusic: LinearLayout = itemView.findViewById(R.id.ll_play_music)
        val ivPausePlay: ImageView = itemView.findViewById(R.id.iv_pause_play_music)

        val tvTotal: TextView = itemView.findViewById(R.id.tv_time_total)
        val ivItemDelete: ImageView = itemView.findViewById(R.id.iv_item_delete)
        val llAudioHeader: LinearLayout = itemView.findViewById(R.id.ll_audio_item_header)
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)

        var playerState: PlayerState = PlayerState.IDLE
        private var isSeekBarStatus = false      // trang thai seekbar co dang duoc keo hay khong
        var filePath: String = ""

        private fun updatePlayInfor(playerInfo: PlayerInfo) {

            val audioFileView = getItem(adapterPosition)
            filePath = playerInfo.currentAudio?.getFilePath().toString()

            playerState = playerInfo.playerState
            sbMusic.max = playerInfo.duration
            sbMusic.progress = playerInfo.posision
            tvTimeLife.text = simpleDateFormat.format(playerInfo.posision)

            when (playerInfo.playerState) {
                PlayerState.PLAYING -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_pause)
                }
                PlayerState.PAUSE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)

                }
                PlayerState.IDLE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
                    tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                    sbMusic.progress = 0
                }
                else -> {
                    //nothing
                }
            }

        }


        override fun onViewAttachedToWindow() {
            super.onViewAttachedToWindow()
            val audioFileView = getItem(adapterPosition)
            audioPlayer.getPlayerInfo().observe(this, object : Observer<PlayerInfo> {
                override fun onChanged(playerInfo: PlayerInfo) {
                    playerInfo.currentAudio?.let {
                        if (!isSeekBarStatus && adapterPosition != -1) {                            // khi summitlist: ham onViewDetachedFromWindow() vua chay va  audioPlayer.getPlayerInfo().observe cung chay nen adapterPosition = -1 (chua kip lay data)
                            if (audioFileView.getFilePath() == it.getFilePath()) {
                                updatePlayInfor(playerInfo)
                            }
                        }
                    }
                }
            })
        }

        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
        }

        @SuppressLint("SetTextI18n")
        fun onBind() {
            val audioFileView = getItem(adapterPosition)
            var bitrate = audioFileView.audioFile.bitRate / 1000
            if (bitrate > 320) {
                bitrate = 320
            }
            tvTitle.setText(audioFileView.audioFile.fileName)
            if (audioFileView.audioFile.size / (1024 * 1024) > 0) {
                tvInfo.setText(String.format("%.1f", (audioFileView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + bitrate + "kb/s")
            } else {
                tvInfo.setText(((audioFileView.audioFile.size) / (1024)).toString() + " KB" + " | " + bitrate + "kb/s")
            }

            tvTotal.text = "/" + simpleDateFormat.format(audioFileView.audioFile.duration.toInt())

            if (audioFileView.isExpanded) {
                llPlayMusic.visibility = View.VISIBLE
                llItem.setBackgroundResource(R.drawable.my_studio_item_bg)
            } else {
                llPlayMusic.visibility = View.GONE
                llItem.setBackgroundColor(Color.WHITE)
                llItem.setPadding(0, 0, 0, 0)
            }

            when (audioFileView.itemLoadStatus.deleteState) {

                DeleteState.HIDE -> {
                    ivItemDelete.visibility = View.GONE
                    ivSetting.visibility = View.VISIBLE
                }
                DeleteState.UNCHECK -> {
                    ivSetting.visibility = View.INVISIBLE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
                }
                DeleteState.CHECKED -> {
                    ivSetting.visibility = View.INVISIBLE
                    ivItemDelete.visibility = View.VISIBLE
                    ivItemDelete.setImageResource(R.drawable.my_studio_screen_icon_checked)
                }
            }

            tvTimeLife.width = Utils.getWidthText(simpleDateFormat.format(audioFileView.audioFile.duration), itemView.context)
                .toInt() + 15

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            ivSetting.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p1: Int, p2: Boolean) {
                    tvTimeLife.text = simpleDateFormat.format(sb?.progress)             // update time cho tvTimeLife khi keo seekbar
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    isSeekBarStatus = true
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    audioPlayer.seek(sbMusic.progress)
                    isSeekBarStatus = false
                }
            })
        }

        override fun onClick(view: View) {
            val audioFileView = getItem(adapterPosition)
            when (view.id) {
                R.id.ll_audio_item_header -> {
                    when (audioFileView.itemLoadStatus.deleteState) {
                        DeleteState.CHECKED -> {
                            audioCutterScreenCallback.checkDeletePos(adapterPosition)
                        }
                        DeleteState.UNCHECK -> {
                            audioCutterScreenCallback.checkDeletePos(adapterPosition)
                        }
                        DeleteState.HIDE -> {
                            audioCutterScreenCallback.isShowPlayingAudio(adapterPosition)
                        }
                    }
                    when (playerState) {
                        PlayerState.IDLE -> {
                        }
                        PlayerState.PAUSE -> {
                            // khi ở trạng thái delete sẽ không stop dc
                            if (audioFileView.itemLoadStatus.deleteState == DeleteState.HIDE) {
                                audioPlayer.stop()
                                playerState = PlayerState.IDLE
                            }
                        }
                        PlayerState.PLAYING -> {
                            if (audioFileView.itemLoadStatus.deleteState == DeleteState.HIDE) {
                                audioPlayer.stop()
                                playerState = PlayerState.IDLE
                            }
                        }
                        else -> {
                            //nothing
                        }
                    }

                }
                R.id.iv_pause_play_music -> {
                    when (playerState) {
                        PlayerState.IDLE -> {
                            lifecycleCoroutineScope.launch {
                                audioPlayer.play(audioFileView.audioFile)
                            }
                        }
                        PlayerState.PAUSE -> {
                            audioPlayer.resume()
                        }
                        PlayerState.PLAYING -> {
                            audioPlayer.pause()
                        }
                        else -> {
                            //nothing
                        }
                    }
                }

                R.id.iv_setting -> {
                    audioCutterScreenCallback.showMenu(view, audioFileView.audioFile)
                }
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : MyStudioHolder(itemView), View.OnClickListener {

        val pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        val ivCancel: ImageView = itemView.findViewById(R.id.iv_cancel)
        val tvWait: TextView = itemView.findViewById(R.id.tv_wait)


        @SuppressLint("SetTextI18n")
        private fun updateItem(convertingItem: ConvertingItem) {

            Log.d(TAG, "updateItem: percent : " + convertingItem.percent + "  status : " + convertingItem.state)
            when (convertingItem.state) {
//                ConvertingState.WAITING -> {
//                    tvWait.visibility = View.VISIBLE
//                    pbLoading.visibility = View.GONE
//                    tvLoading.visibility = View.GONE
//                }
                ConvertingState.PROGRESSING -> {
//                    tvWait.visibility = View.GONE
//                    pbLoading.visibility = View.VISIBLE
//                    tvLoading.visibility = View.VISIBLE
                    pbLoading.max = 100
                    pbLoading.progress = convertingItem.percent
                    tvLoading.text = convertingItem.percent.toString() + "%"
                }

                ConvertingState.ERROR -> {
                    audioCutterScreenCallback.errorConverting(convertingItem.getFileName())
                }

                else -> {
                    //nothing
                }
            }
        }

        override fun onViewAttachedToWindow() {
            super.onViewAttachedToWindow()
            val audioFileView = getItem(adapterPosition)
            audioEditorManager.getCurrentProcessingItem()
                .observe(this, object : Observer<ConvertingItem?> {
                    override fun onChanged(convertingItem: ConvertingItem?) {
                        convertingItem?.let {
                            if (adapterPosition != -1) {
                                val itemView = getItem(adapterPosition)
                                if (itemView.id == it.id) {
                                    updateItem(it)
                                }
                            }
                        }

                    }
                })
        }

        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
        }

        @SuppressLint("SetTextI18n")
        fun onBind() {
            val loadingItem = getItem(adapterPosition)

            when (loadingItem.convertingState) {
                ConvertingState.WAITING -> {
                    tvWait.visibility = View.VISIBLE
                    pbLoading.visibility = View.GONE
                    tvLoading.visibility = View.GONE
                }
                ConvertingState.PROGRESSING -> {
                    tvWait.visibility = View.GONE
                    pbLoading.visibility = View.VISIBLE
                    tvLoading.visibility = View.VISIBLE
                    pbLoading.max = 100
                    pbLoading.progress = loadingItem.percent
                    tvLoading.text = loadingItem.percent.toString() + "%"
                }
                else -> {
                    //nothing
                }
            }

            tvTitle.setText(loadingItem.audioFile.fileName)
            ivCancel.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val loadingItem = getItem(adapterPosition)
            when (view.id) {
                R.id.iv_cancel -> {
                    audioCutterScreenCallback.cancelLoading(loadingItem.id)
                    Log.d(TAG, "onClick: ")
                }
            }
        }
    }

    companion object {
        const val SUCCESS_VIEW = 0
        const val LOADING_VIEW = 1
    }
}

class MusicDiffCallBack : DiffUtil.ItemCallback<AudioFileView>() {

    override fun areItemsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView.audioFile.file.absoluteFile == newItemView.audioFile.file.absoluteFile
    }

    override fun areContentsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: AudioFileView, newItem: AudioFileView): Any? {

        return newItem
    }
}
package com.example.audiocutter.functions.mystudio.adapters

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AudioEditorManager
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.objects.DeleteState
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.my_studio_screen_item.view.*
import kotlinx.coroutines.launch

interface AudioCutterScreenCallback {
    fun showMenu(view: View, audioFile: AudioFile)
    fun checkDeletePos(position: Int)
    fun isShowPlayingAudio(position: Int)
    fun cancelLoading(id: Int)
    fun errorConverting(fileName: String)
}

class AudioCutterAdapter(val audioCutterScreenCallback: AudioCutterScreenCallback, val audioPlayer: AudioPlayer, val audioEditorManager: AudioEditorManager, val lifecycleCoroutineScope: LifecycleCoroutineScope) : ListAdapter<AudioFileView, AudioCutterAdapter.MyStudioHolder>(MusicDiffCallBack()) {

    private val TAG = "giangtd"

    private lateinit var recyclerView: RecyclerView
    private var sbAnimation: ObjectAnimator? = null
    private var progressbarAnimation: ObjectAnimator? = null
    private val DURATION_ANIMATION = 500L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStudioHolder {
        recyclerView = parent as RecyclerView
        return if (viewType == SUCCESS_VIEW) {
            SuccessViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.my_studio_screen_item, parent, false))
        } else LoadingViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_item_loading, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        Log.d(TAG, "getItemViewType: stauts : " + getItem(position).convertingState)
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
//        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            Log.d(TAG, "onBindViewHolder: orange 1")
            super.onBindViewHolder(holder, position, payloads)
        } else {

            val data = payloads.firstOrNull()
            data?.let {
                val newItem = data as AudioFileView
                Log.d(TAG, "onBindViewHolder: orange 2" + " status: " + newItem.convertingState)
                if (holder is SuccessViewHolder) {

//                val diff = payloads.get(0) as Bundle          // lay ra data duoc thay doi o ham payload va update len view
//                if(diff.containsKey("item1")){
//
//                }
//                if(diff.containsKey("item2")){
//
//                }
//                if(diff.containsKey("item3")){
//
//                }
                    val successViewHolder = holder as SuccessViewHolder
                    when (newItem.itemLoadStatus.deleteState) {
                        DeleteState.HIDE -> {
                            successViewHolder.ivItemDelete.visibility = View.INVISIBLE
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
                        successViewHolder.sbMusic.clearAnimation()
                        sbAnimation?.cancel()

                    } else {
                        successViewHolder.llPlayMusic.visibility = View.GONE
                        successViewHolder.llItem.setBackgroundColor(Color.WHITE)
                        successViewHolder.llItem.setPadding(0, 0, 0, 0)
                    }

                } else {
                    Log.d(TAG, "onBindViewHolder1 LoadingViewHolder ${newItem.getFilePath()} ")
                    val loadingViewHolder = holder as LoadingViewHolder

                    val convertingItem = getItem(position)

                    Log.d(TAG, "onBindViewHolder: convertingItem.percent : " + convertingItem.percent)

                    when (newItem.convertingState) {
                        ConvertingState.WAITING -> {
                            loadingViewHolder.tvWait.visibility = View.VISIBLE
                            loadingViewHolder.pbLoading.visibility = View.INVISIBLE
                            loadingViewHolder.tvLoading.visibility = View.INVISIBLE
                        }
                        ConvertingState.PROGRESSING -> {
                            loadingViewHolder.tvWait.visibility = View.INVISIBLE
                            loadingViewHolder.pbLoading.visibility = View.VISIBLE
                            loadingViewHolder.tvLoading.visibility = View.VISIBLE
                        }
                        else -> {
                            //TODO
                        }
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
        var timeFomat = 0
        private fun updatePlayInfor(playerInfo: PlayerInfo) {

            val audioFileView = getItem(adapterPosition)
            filePath = playerInfo.currentAudio?.getFilePath().toString()

//            sbMusic.max = playerInfo.duration * 100

            timeFomat = Utils.chooseTimeFormat(playerInfo.duration.toLong())
            tvTimeLife.text = Utils.toTimeStr(playerInfo.posision.toLong(), timeFomat)

            when (playerInfo.playerState) {
                PlayerState.PLAYING -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_pause)
                    setSeekbarAnimate(sbMusic, playerInfo.posision, DURATION_ANIMATION)

                }
                PlayerState.PAUSE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
                }
                PlayerState.IDLE -> {
                    itemView.iv_pause_play_music.setImageResource(R.drawable.my_studio_item_icon_play)
                    tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                    sbMusic.clearAnimation()
                    sbAnimation?.cancel()
                    sbMusic.progress = 0
                    setSeekbarAnimate(sbMusic, 0, DURATION_ANIMATION)
                }
                else -> {
                    //nothing
                }
            }
        }

        fun setSeekbarAnimate(pb: SeekBar, progressTo: Int, duration: Long) {
            // smooth animation

            Log.d(TAG, "setSeekbarAnimate: pb.progress : " + pb.progress + " progressTo : " + progressTo + "max ${pb.max}")
            sbAnimation?.cancel()
            sbAnimation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
            sbAnimation?.setDuration(duration)
            sbAnimation?.setInterpolator(DecelerateInterpolator())
            sbAnimation?.start()
        }

        override fun onViewAttachedToWindow() {
            super.onViewAttachedToWindow()
            val audioFileView = getItem(adapterPosition)
            audioPlayer.getPlayerInfo().observe(this, object : Observer<PlayerInfo> {
                override fun onChanged(playerInfo: PlayerInfo) {
                    playerState = playerInfo.playerState
                    Log.d(TAG, "onStopTrackingTouch: status 1: " + playerState)
                    Log.d(TAG, " progressbar : 2" + playerInfo.posision + " duration : " + playerInfo.duration)
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
            sbAnimation?.cancel()
        }

        @SuppressLint("SetTextI18n")
        fun onBind() {

            val audioFileView = getItem(adapterPosition)
            var bitrate = audioFileView.audioFile.bitRate / 1000

            sbMusic.max = audioFileView.audioFile.duration.toInt() * 100

            tvTitle.setText(audioFileView.audioFile.fileName)
            if (audioFileView.audioFile.size / (1024 * 1024) > 0) {
                tvInfo.setText(String.format("%.1f", (audioFileView.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + bitrate + "kb/s")
            } else {
                tvInfo.setText(((audioFileView.audioFile.size) / (1024)).toString() + " KB" + " | " + bitrate + "kb/s")
            }

            timeFomat = Utils.chooseTimeFormat(audioFileView.audioFile.duration)

            tvTimeLife.width = tvTimeLife.paint.measureText(Utils.toTimeStr(audioFileView.audioFile.duration, timeFomat))
                .toInt()
            tvTotal.width = tvTotal.paint.measureText("/" + Utils.toTimeStr(audioFileView.audioFile.duration, timeFomat))
                .toInt()

            tvTotal.text = "/" + Utils.toTimeStr(audioFileView.audioFile.duration, timeFomat)

            if (audioFileView.audioFile.bitmap != null) {
                Glide.with(itemView).load(audioFileView.audioFile.bitmap)
//                    .transform(RoundedCorners(Utils.convertDp2Px(4, itemView.context).toInt()))
                    .into(ivAvatar)

            } else {
                ivAvatar.setImageResource(R.drawable.my_studio_item_ic_avatar)
            }

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

            llAudioHeader.setOnClickListener(this)

            ivPausePlay.setOnClickListener(this)

            ivSetting.setOnClickListener(this)

            sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {

                    Log.d(TAG, "onProgressChanged: aloha progress : " + progress)
                    Log.d(TAG, "onProgressChanged: PlayerState.PAUSE ${playerState}")
                    if (playerState != PlayerState.IDLE) {
                        if (fromUser) {
                            sbMusic.clearAnimation()
                            sbAnimation?.cancel()
                            setSeekbarAnimate(sbMusic, progress / 100, DURATION_ANIMATION)
                        }
                    }
                    if (playerState == PlayerState.PAUSE) {
                        tvTimeLife.text = Utils.toTimeStr(progress.toLong() / 100, timeFomat)
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {
                    Log.d(TAG, "onStartTrackingTouch: status 2" + playerState)
                    Log.d(TAG, "setSeekbarAnimate: pb.progress : " + sbMusic.progress + " max : " + sbMusic.max)
                    if (playerState == PlayerState.PLAYING) {

                        audioPlayer.pause()
                        sbMusic.clearAnimation()
                        sbAnimation?.cancel()
                        isSeekBarStatus = true
                    }
                }

                override fun onStopTrackingTouch(sb: SeekBar?) {
                    Log.d(TAG, "onStopTrackingTouch: status 3: " + playerState)
                    Log.d(TAG, " progressbar : 2" + sbMusic.progress)
                    if (playerState == PlayerState.IDLE) {
//                        lifecycleScope.launch {
                            sbMusic.clearAnimation()
                            sbAnimation?.cancel()
                            val newValue = Utils.convertValue(0, sbMusic.max, 0, audioFileView.audioFile.duration.toInt(), sbMusic.progress)
                            Log.d(TAG, "checkNewValue: $newValue  - duration ${audioFileView.audioFile.duration.toInt()} ")
                            audioPlayer.play(audioFileView.audioFile, newValue)
//                        }
                    } else {

//                        Log.d(TAG, "onStopTrackingTouch: status 4: " + playerState)
//                        sbMusic.clearAnimation()
//                        sbAnimation?.cancel()
//                        audioPlayer.seek(sbMusic.progress / 100)
//                        audioPlayer.resume()
//                        isSeekBarStatus = false
                    }

                    Log.d(TAG, "onStopTrackingTouch: status 4: " + playerState)
                    sbMusic.clearAnimation()
                    sbAnimation?.cancel()
                    audioPlayer.seek(sbMusic.progress / 100)
                    audioPlayer.resume()
                    isSeekBarStatus = false
                }

            })  // sb
        }

        override fun onClick(view: View) {
            try {
                val audioFileView = getItem(adapterPosition)
                when (view.id) {
                    R.id.ll_audio_item_header -> {

                        sbMusic.progress = 0
                        ivPausePlay.setImageResource(R.drawable.my_studio_item_icon_play)
                        tvTimeLife.text = Constance.TIME_LIFE_DEFAULT

                        sbMusic.clearAnimation()
                        sbAnimation?.cancel()

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

                        Log.d(TAG, "onClick: status : " + playerState)
                        when (playerState) {
                            PlayerState.IDLE -> {
                                lifecycleCoroutineScope.launch {
                                    audioPlayer.play(audioFileView.audioFile)
                                    Log.d(TAG, "onClick: audioFileView.audioFile uri : " + audioFileView.audioFile.uri)
                                    sbMusic.clearAnimation()
                                    sbAnimation?.cancel()
                                    sbMusic.progress = 0
                                    setSeekbarAnimate(sbMusic, 0, DURATION_ANIMATION)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    inner class LoadingViewHolder(itemView: View) : AudioCutterAdapter.MyStudioHolder(itemView), View.OnClickListener {

        val pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        val ivCancel: ImageView = itemView.findViewById(R.id.iv_cancel)
        val tvWait: TextView = itemView.findViewById(R.id.tv_wait)


        @SuppressLint("SetTextI18n")
        private fun updateItem(convertingItem: ConvertingItem) {

            Log.d(TAG, "updateItem: percent : " + convertingItem.percent + "  status : " + convertingItem.state)
            when (convertingItem.state) {

                ConvertingState.PROGRESSING -> {

                    setProgressAnimate(pbLoading, convertingItem.percent, DURATION_ANIMATION)

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
            pbLoading.max = 100 * 100
            when (loadingItem.convertingState) {
                ConvertingState.WAITING -> {
                    tvWait.visibility = View.VISIBLE
                    pbLoading.visibility = View.INVISIBLE
                    tvLoading.visibility = View.INVISIBLE
                }
                ConvertingState.PROGRESSING -> {
                    tvWait.visibility = View.INVISIBLE
                    pbLoading.visibility = View.VISIBLE
                    tvLoading.visibility = View.VISIBLE
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


        fun setProgressAnimate(pb: ProgressBar, progressTo: Int, duration: Long) {
            // smooth animation
            progressbarAnimation?.cancel()
            progressbarAnimation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
            progressbarAnimation?.setDuration(duration)
            progressbarAnimation?.setInterpolator(DecelerateInterpolator())
            progressbarAnimation?.start()
        }

    }

    companion object {
        const val SUCCESS_VIEW = 0
        const val LOADING_VIEW = 1
    }
}

class MusicDiffCallBack : DiffUtil.ItemCallback<AudioFileView>() {

    override fun areItemsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {     //true      :  item da dc khoi tao --> vao onbind summitlist
//        return oldItemView.audioFile.file.absoluteFile == newItemView.audioFile.file.absoluteFile     // falase   :  item chua duoc khoi tao --> vao onBind cua supper
        return oldItemView.audioFile == newItemView.audioFile
    }

    override fun areContentsTheSame(oldItemView: AudioFileView, newItemView: AudioFileView): Boolean {      // true  : item giong nhau hoan toan khong co j can update
        return oldItemView == newItemView                                                                   // false : item co su thay doi se vao ham onBind summitlist
    }

    override fun getChangePayload(oldItem: AudioFileView, newItem: AudioFileView): Any? {       // chay duoi background
        val diff = Bundle()                                                                     // xu ly logic o day
        // olditem.item1 != newItem.item1
        //diff.putString("item1", newItem.item1)
        // olditem.item2 != newItem.item2
        //diff.putString("item2", newItem.item2)
        // olditem.item3 != newItem.item3
        //diff.putString("item3", newItem.item3)

//        return diff
        return newItem
    }
}
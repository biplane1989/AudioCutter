package com.example.audiocutter.functions.resultscreen.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.ResultScreenBinding
import com.example.audiocutter.functions.contacts.screens.ListContactViewModel
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import java.text.SimpleDateFormat

class ResultScreen : BaseFragment(), View.OnClickListener {

    val TAG = "giangtd"
    private lateinit var binding: ResultScreenBinding
    var audioId = 0
    lateinit var audioStatus: ConvertingState
    lateinit var mResultViewModel: ResultViewModel
    var idProgress = -1
    private var simpleDateFormat = SimpleDateFormat("mm:ss")
    var playerState: PlayerState = PlayerState.IDLE


    @SuppressLint("SetTextI18n")
    val processObserver = Observer<ConvertingItem> { data ->

//        if (data.id == 1) {
        if (data.state == ConvertingState.PROGRESSING) {
            binding.pbLoading.progress = data.percent
            binding.tvLoading.text = data.percent.toString() + "%"
            binding.tvTitleMusic.text = data.audioFile.fileName

            if (data.audioFile.size / (1024 * 1024) > 0) {

                binding.tvInfoMusic.setText(String.format("%.1f", (data.audioFile.size) / (1024 * 1024).toDouble()) + " MB" + " | " + data.audioFile.bitRate.toString() + "kb/s")
            } else {
                binding.tvInfoMusic.setText(((data.audioFile.size) / (1024)).toString() + " KB" + " | " + data.audioFile.bitRate.toString() + "kb/s")
            }

            data.audioFile.bitmap?.let {
                binding.ivAvatarMusic.setImageBitmap(it)
            }

        } else {
            binding.llProgressbar.visibility = View.GONE
            binding.llPlayMusic.visibility = View.VISIBLE
        }
//        }
    }


    val playInfoObserver = Observer<PlayerInfo> { playInfo ->

        binding.sbMusic.max = playInfo.duration
        binding.sbMusic.progress = playInfo.posision
        binding.tvTimeTotal.text = "/" + simpleDateFormat.format(playInfo.duration)
        binding.tvTimeLife.text = simpleDateFormat.format(playInfo.posision)

        playerState = playInfo.playerState
    }

    val listAudioObserver = Observer<List<ConvertingItem>> { it ->
        Log.d(TAG, "audio size : " + it.size)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)

        mResultViewModel.getData().observe(viewLifecycleOwner, processObserver)
        mResultViewModel.getPlayerInfo().observe(viewLifecycleOwner, playInfoObserver)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mResultViewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)

        idProgress = mResultViewModel.getIDProgressItem()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbLoading.max = 100

        binding.ivPausePlayMusic.setOnClickListener(this)

        binding.llRingtone.setOnClickListener(this)

//        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
//            .observe(viewLifecycleOwner, processObserver)

        /*
        ManagerFactory.getAudioEditorManager().getListCuttingItems()
            .observe(viewLifecycleOwner, listAudioObserver)

        binding.pbResult.max = 100

        binding.btnDelete.setOnClickListener(View.OnClickListener {
            audioStatus?.let {
                if (audioStatus == ConvertingState.PROGRESSING) {
                    ManagerFactory.getAudioEditorManager().cancel(audioId)
                }
            }
        })*/

        /* var isPlay = false
         binding.ivPausePlayMusic.setOnClickListener(View.OnClickListener {
             isPlay = !isPlay
             if (isPlay) {

             } else {
                 binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                 mResultViewModel.pauseAudio()
             }
         })*/
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        mResultViewModel.stopAudio()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivPausePlayMusic -> {
                when (playerState) {
                    PlayerState.IDLE -> {
                        binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
                        mResultViewModel.playAudio()
                    }
                    PlayerState.PAUSE -> {
                        binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
                        mResultViewModel.resumeAudio()
                    }

                    PlayerState.PLAYING -> {
                        binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                        mResultViewModel.pauseAudio()
                    }
                }
            }
        }
    }

}
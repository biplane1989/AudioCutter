package com.example.audiocutter.functions.resultscreen.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
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

        if (data.id == ManagerFactory.getAudioEditorManager().getIDProcessingItem()) {
            when (data.state) {
                ConvertingState.WAITING -> {
                    binding.tvWait.visibility = View.VISIBLE
                    binding.llProgressbar.visibility = View.GONE
                    binding.llPlayMusic.visibility = View.GONE
                    binding.clOpption.visibility = View.GONE
                }
                ConvertingState.PROGRESSING -> {
                    binding.tvWait.visibility = View.GONE
                    binding.clOpption.visibility = View.GONE
                    binding.llProgressbar.visibility = View.VISIBLE
                    binding.llPlayMusic.visibility = View.GONE

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

                }
                ConvertingState.SUCCESS -> {
                    binding.tvWait.visibility = View.GONE
                    binding.llProgressbar.visibility = View.GONE
                    binding.llPlayMusic.visibility = View.VISIBLE
                    binding.clOpption.visibility = View.VISIBLE
                }
            }
        }else{
            binding.tvWait.visibility = View.VISIBLE
            binding.llProgressbar.visibility = View.GONE
            binding.llPlayMusic.visibility = View.GONE
            binding.clOpption.visibility = View.GONE
        }

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

        binding.sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                mResultViewModel.seekToAudio(sb!!.progress)
            }
        })
    }

    override fun onDestroyView() {
        mResultViewModel.stopAudio()
        super.onDestroyView()
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
package com.example.audiocutter.functions.resultscreen.screens

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.databinding.ResultScreenBinding
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
    lateinit var convertingItem: ConvertingItem

    @SuppressLint("SetTextI18n")
    val processObserver = Observer<ConvertingItem> { data ->
        data?.let {
            if (it.id == ManagerFactory.getAudioEditorManager().getIDProcessingItem()) {
                Log.d(TAG, "ConvertingState : " + it.state + " ID : " + it.id + " IDProcessingItem " + ManagerFactory.getAudioEditorManager()
                    .getIDProcessingItem())
                when (it.state) {
                    ConvertingState.WAITING -> {
                        binding.tvWait.visibility = View.VISIBLE
                        binding.llProgressbar.visibility = View.GONE
                        binding.llPlayMusic.visibility = View.GONE
                        binding.clOpption.visibility = View.GONE
                        binding.btnBack.visibility = View.GONE
                        binding.ivHome.visibility = View.INVISIBLE
                    }
                    ConvertingState.PROGRESSING -> {
                        binding.btnBack.visibility = View.GONE
                        binding.ivHome.visibility = View.INVISIBLE
                        binding.tvWait.visibility = View.GONE
                        binding.clOpption.visibility = View.GONE
                        binding.llProgressbar.visibility = View.VISIBLE
                        binding.llPlayMusic.visibility = View.GONE

                        binding.pbLoading.progress = data.percent
                        binding.tvLoading.text = data.percent.toString() + "%"
                        binding.tvTitleMusic.text = data.fileName
                        binding.tvInfoMusic.text = data.audioFile.bitRate.toString() + "kb/s"


                        data.audioFile.bitmap?.let {
                            binding.ivAvatarMusic.setImageBitmap(it)
                        }

                    }
                    ConvertingState.SUCCESS -> {
                        binding.btnBack.visibility = View.VISIBLE
                        binding.ivHome.visibility = View.VISIBLE
                        binding.btnCancel.visibility = View.INVISIBLE
                        binding.tvWait.visibility = View.GONE
                        binding.llProgressbar.visibility = View.GONE
                        binding.llPlayMusic.visibility = View.VISIBLE
                        binding.clOpption.visibility = View.VISIBLE
                        binding.btnOrigin.visibility = View.GONE
                        binding.tvTitleResult.visibility = View.VISIBLE
                        binding.tvTitleLoading.visibility = View.GONE

//                        binding.tvTitleMusic.text = convertingItem.audioFile.fileName
//                        binding.tvInfoMusic.text = convertingItem.audioFile.bitRate.toString() + "kb/s"
//                        binding.tvTimeTotal.text = "/" + simpleDateFormat.format(convertingItem.audioFile.size)
//                        data.audioFile.bitmap?.let {
//                            binding.ivAvatarMusic.setImageBitmap(it)
//                        }

//                        if (it.audioFile.size / (1024f * 1024) > 0) {
//                            binding.tvInfoMusic.setText(String.format("%.1f", (it.audioFile.size) / (1024f * 1024)) + " MB" + " | " + it.audioFile.bitRate.toString() + "kb/s")
//                        } else {
//                            binding.tvInfoMusic.setText(((it.audioFile.size) / (1024f)).toString() + " KB" + " | " + it.audioFile.bitRate.toString() + "kb/s")
//                        }
                    }
                    else -> {
                    }
                }
            } else {
                binding.tvWait.visibility = View.VISIBLE
                binding.llProgressbar.visibility = View.GONE
                binding.llPlayMusic.visibility = View.GONE
                binding.clOpption.visibility = View.GONE
            }
        }
    }

    val itemConvertingItemObserver = Observer<ConvertingItem> { it ->       // observer 1 item tu core
        convertingItem = ConvertingItem(it.id, it.state, it.percent, it.audioFile, it.fileName)
        Log.d("009", "ConvertingItem Observer path: " + it.audioFile.file.absoluteFile)
        if (convertingItem.state == ConvertingState.SUCCESS) {
            binding.tvTitleMusic.text = convertingItem.audioFile.fileName
            binding.tvInfoMusic.text = convertingItem.audioFile.bitRate.toString() + "kb/s"

            val duration = ManagerFactory.getAudioFileManager()
                .getInfoAudioFile(convertingItem.audioFile.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!duration.isNullOrBlank()) {
                binding.tvTimeTotal.text = "/" + simpleDateFormat.format(duration.toInt())
            }
            convertingItem.audioFile.bitmap?.let {
                binding.ivAvatarMusic.setImageBitmap(it)
            }

            if (convertingItem.audioFile.size / (1024f * 1024) > 0) {
                binding.tvInfoMusic.setText(String.format("%.1f", (convertingItem.audioFile.size) / (1024f * 1024)) + " MB" + " | " + convertingItem.audioFile.bitRate.toString() + "kb/s")
            } else {
                binding.tvInfoMusic.setText(((convertingItem.audioFile.size) / (1024f)).toString() + " KB" + " | " + convertingItem.audioFile.bitRate.toString() + "kb/s")
            }
        }
    }

    val playInfoObserver = Observer<PlayerInfo> { playInfo ->

        binding.sbMusic.max = playInfo.duration
        binding.sbMusic.progress = playInfo.posision
        binding.tvTimeTotal.text = "/" + simpleDateFormat.format(playInfo.duration)
        binding.tvTimeLife.text = simpleDateFormat.format(playInfo.posision)

        playerState = playInfo.playerState
        Log.d("009", "playInfoObserver playerState: " + playerState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)

//        mResultViewModel.getData().observe(viewLifecycleOwner, processObserver)
        AudioEditorManagerlmpl.getCurrentProcessingItem()
            .observe(viewLifecycleOwner, processObserver)
        AudioEditorManagerlmpl.getConvertingItem()
            .observe(viewLifecycleOwner, itemConvertingItemObserver)
        mResultViewModel.getPlayerInfo().observe(viewLifecycleOwner, playInfoObserver)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mResultViewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)

//        idProgress = mResultViewModel.getIDProgressItem()
        idProgress = ManagerFactory.getAudioEditorManager().getIDProcessingItem()
        Log.d(TAG, "idProgress: " + idProgress)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbLoading.max = 100

        binding.ivPausePlayMusic.setOnClickListener(this)

        binding.llRingtone.setOnClickListener(this)
        binding.llAlarm.setOnClickListener(this)
        binding.llNotification.setOnClickListener(this)
        binding.llShare.setOnClickListener(this)
        binding.llContact.setOnClickListener(this)
        binding.llOpenwith.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.ivHome.setOnClickListener(this)
        binding.btnOrigin.setOnClickListener(this)

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

    override fun onPostDestroy() {
        mResultViewModel.stopAudio()
        super.onPostDestroy()
        Log.d("009", "onPostDestroy: ")
    }

    override fun onDestroyView() {
        Log.d("009", "onDestroyView: ")
        mResultViewModel.stopAudio()
        super.onDestroyView()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivPausePlayMusic -> {
                Log.d("009", "onClick: onClick "+ " Status: "+ playerState + "  file path : "+ convertingItem.audioFile.file.absoluteFile )
                when (playerState) {
                    PlayerState.IDLE -> {
                        binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
                        mResultViewModel.playAudio(convertingItem)
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

            binding.btnCancel -> {
                ManagerFactory.getAudioEditorManager()
                    .cancel(ManagerFactory.getAudioEditorManager().getIDProcessingItem())
                requireActivity().onBackPressed()
            }

            binding.btnBack -> {
                requireActivity().onBackPressed()
            }

            binding.ivHome -> {
                viewStateManager.resultScreenGoToHome(requireContext())
            }

            binding.btnOrigin -> {
                viewStateManager.resultScreenGoToHome(requireContext())
            }
            binding.llRingtone -> {
                if (ManagerFactory.getRingtoneManager().setRingTone(convertingItem.audioFile)) {
                    Toast.makeText(requireContext(), "Set Ringtone Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Ringtone Fail !", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            binding.llAlarm -> {
                if (ManagerFactory.getRingtoneManager().setAlarmManager(convertingItem.audioFile)) {
                    Toast.makeText(requireContext(), "Set Alarm Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Alarm Fail !", Toast.LENGTH_SHORT).show()
                }
            }

            binding.llNotification -> {
                if (ManagerFactory.getRingtoneManager()
                        .setNotificationSound(convertingItem.audioFile)) {
                    Toast.makeText(requireContext(), "Set Notification Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Notification Fail !", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.llShare -> {

            }
            binding.llContact -> {

            }
            binding.llOpenwith -> {

            }
        }
    }

}
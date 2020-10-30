package com.example.audiocutter.functions.resultscreen.screens

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
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.databinding.ResultScreenBinding
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.functions.resultscreen.objects.ConvertingState
import com.example.audiocutter.objects.AudioFile
import java.lang.StringBuilder
import java.text.SimpleDateFormat


class ResultScreen : BaseFragment(), View.OnClickListener {
    companion object {
        const val MIX = 3
        const val MER = 2
        const val CUT = 1
    }

    private val safeArg: ResultScreenArgs by navArgs()
    val TAG = "giangtd"
    private lateinit var binding: ResultScreenBinding
    lateinit var mResultViewModel: ResultViewModel
    private var simpleDateFormat = SimpleDateFormat("mm:ss")

    val processDoneObserver = Observer<AudioFile> {         // observer trang thai done
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

        binding.tvTitleMusic.text = it.fileName
        binding.tvInfoMusic.text = String.format("%s kb/s", it.bitRate.toString())

        val duration = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(it.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (!duration.isNullOrBlank()) {
            binding.tvTimeTotal.text = String.format("/%s", simpleDateFormat.format(duration.toInt()))
        }
        binding.tvInfoMusic.setText(convertAudioSizeToString(it))

    }
    val pendingProcessObserver = Observer<String> {     // observer trang thai pending
        binding.tvWait.visibility = View.VISIBLE
        binding.llProgressbar.visibility = View.GONE
        binding.llPlayMusic.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.btnBack.visibility = View.GONE
        binding.ivHome.visibility = View.INVISIBLE
        binding.btnCancel.visibility = View.VISIBLE
    }
    val processingObserver = Observer<ConvertingItem> {     // observer trang thai processing
        binding.btnBack.visibility = View.GONE
        binding.ivHome.visibility = View.INVISIBLE
        binding.tvWait.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.llProgressbar.visibility = View.VISIBLE
        binding.llPlayMusic.visibility = View.GONE
        binding.btnCancel.visibility = View.VISIBLE

        binding.pbLoading.progress = it.percent
        binding.tvLoading.text = it.percent.toString() + "%"
        binding.tvTitleMusic.text = it.getFileName()
        binding.tvInfoMusic.text = it.bitRate.toString() + "kb/s"
    }

    private fun convertAudioSizeToString(audioFile: AudioFile): String {
        var formatSize = "MB"
        var sizeValue = 0f
        if (audioFile.size < (1024 * 1024)) {
            formatSize = "KB"
            sizeValue = audioFile.size / 1024f
        } else {
            sizeValue = audioFile.size / (1024f * 1024)
        }

        val str = StringBuilder()
        str.append(String.format("%.1f", sizeValue))
        str.append(" ")
        str.append(formatSize)
        str.append(" | ")
        str.append(audioFile.bitRate.toString())
        str.append("kb/s")
        return str.toString()
    }

    val playInfoObserver = Observer<PlayerInfo> { playInfo ->       // observer info play music
        playInfo.playerState
        binding.sbMusic.max = playInfo.duration
        binding.sbMusic.progress = playInfo.posision
        binding.tvTimeTotal.text = "/" + simpleDateFormat.format(playInfo.duration)
        binding.tvTimeLife.text = simpleDateFormat.format(playInfo.posision)

        when (playInfo.playerState) {
            PlayerState.IDLE -> {
                binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
            }
            PlayerState.PAUSE -> {
                binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
            }
            PlayerState.PLAYING -> {
                binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)
        mResultViewModel.getPendingProcessLiveData()
            .observe(viewLifecycleOwner, pendingProcessObserver)
        mResultViewModel.getProcessingLiveData().observe(viewLifecycleOwner, processingObserver)
        mResultViewModel.getProcessDoneLiveData().observe(viewLifecycleOwner, processDoneObserver)
        mResultViewModel.getPlayerInfo().observe(viewLifecycleOwner, playInfoObserver)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mResultViewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)
        mResultViewModel.init(safeArg)
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

    override fun onClick(view: View?) {
        when (view) {
            binding.ivPausePlayMusic -> {
                runOnUI {
                    mResultViewModel.playAudio()
                }
            }

            binding.btnCancel -> {
                ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.let {
                    ManagerFactory.getAudioEditorManager().cancel(it.id)
                }
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
                if (mResultViewModel.setRingTone()) {
                    Toast.makeText(requireContext(), "Set Ringtone Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Ringtone Fail !", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.llAlarm -> {

                if (mResultViewModel.setAlarm()) {
                    Toast.makeText(requireContext(), "Set Alarm Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Alarm Fail !", Toast.LENGTH_SHORT).show()
                }

            }

            binding.llNotification -> {
                if (mResultViewModel.setNotification()) {
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
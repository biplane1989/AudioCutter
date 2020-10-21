package com.example.audiocutter.functions.editor.screen


import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.MixingScreenBinding
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.audiochooser.mix.ChangeRangeView
import com.example.audiocutter.util.Utils

class MixingScreen : BaseFragment(), View.OnClickListener, ChangeRangeView.OnPlayLineChange {
    private val mPlayer1 = ManagerFactory.newAudioPlayer()
    private val mPlayer2 = ManagerFactory.newAudioPlayer()
    private var durAudio2: String = ""
    private var durAudio1: String = ""
    private var durAudioMax: String = ""
    private val TAG = MixingScreen::class.java.name
    private var playerState = PlayerState.IDLE
    private lateinit var binding: MixingScreenBinding
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile
    private var isCompare = false


    private val listData = mutableListOf<AudioFile>()


    override fun onPostCreate(savedInstanceState: Bundle?) {
        /** setting maxdistance*/
        mPlayer1.init(requireContext())
        mPlayer2.init(requireContext())

        super.onPostCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mixing_screen, container, false)
        initViews()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//                mPlayer1.getPlayerInfo().observe(viewLifecycleOwner, observerAudio())

        mPlayer2.getPlayerInfo().observe(viewLifecycleOwner, observerAudio())
        mPlayer1.getPlayerInfo().observe(viewLifecycleOwner, observerAudio())

    }


    private fun observerAudio(): Observer<PlayerInfo> {
        return Observer {
            when (it.playerState) {
                PlayerState.IDLE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.IDLE
                }
                PlayerState.PREPARING -> {
                }
                PlayerState.PLAYING -> {
                    binding.crChangeViewMixing.setPosition(it.posision)
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_pause_ic)
                    playerState = PlayerState.PLAYING
                }
                PlayerState.PAUSE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.PAUSE
                }
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun initViews() {
//        audioFile1 = listData[0]
//        audioFile2 = listData[1]

        audioFile1 = ManagerFactory.getAudioFileManager()
            .buildAudioFile("/storage/emulated/0/Download/Ed Sheeran - Shape Of You [Official].mp3  ")
        audioFile2 = ManagerFactory.getAudioFileManager()
            .buildAudioFile("/storage/emulated/0/Download/Lalala-LilKnight_3hy9.mp3")
        durAudio1 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(
                audioFile1.file,
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )!!

        durAudio2 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(
                audioFile2.file,
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )!!
        listData.add(audioFile1)
        listData.add(audioFile2)




        binding.crChangeViewMixing.setFileAudio(audioFile1, audioFile2)

        runOnUI {
            isCompare = durAudio1.toInt() > durAudio2.toInt()
            durAudioMax = if (isCompare) {
                durAudio1
            } else {
                durAudio2
            }
            binding.crChangeViewMixing.setLengthAudio(durAudio1, durAudio2)
            Log.d(
                "TAG",
                "setLengthAudio1 :lenggth1 $durAudio1  - length2 $durAudio2   iscompare $isCompare"
            )
        }


        binding.crChangeViewMixing.mCallback = this
        binding.playIv.setOnClickListener(this)
        binding.shortedBt.setOnClickListener(this)
        binding.longestBt.setOnClickListener(this)
        binding.ivNextMixing.setOnClickListener(this)
        binding.ivPreviousMixing.setOnClickListener(this)
        binding.ivBackMixing.setOnClickListener(this)
        binding.ivDoneMixing.setOnClickListener(this)


    }

    override fun onPause() {
        super.onPause()
        mPlayer1.pause()
        mPlayer2.pause()
    }


    override fun onClick(v: View) {
        when (v) {
            binding.playIv -> {
                runOnUI {
                    if (playerState == PlayerState.PLAYING) {
                        mPlayer1.pause()
                        mPlayer2.pause()
                    } else {
                        if (playerState == PlayerState.IDLE) {
                            mPlayer1.play(audioFile1)
                            mPlayer2.play(audioFile2)

                        } else {
                            mPlayer2.resume()
                            mPlayer1.resume()
                        }
                    }
                }
            }
            binding.shortedBt -> {
                checkCompareDuration(durAudio1, durAudio2)

            }
            binding.longestBt -> {
                checkCompareDurationMin(durAudio1, durAudio2)
            }
            binding.ivNextMixing -> {
                binding.crChangeViewMixing.seekNext5S(5000)
            }
            binding.ivPreviousMixing -> {
                binding.crChangeViewMixing.seekPrev5S(5000)
            }
            binding.ivBackMixing ->{
                showToast("back frg")
            }
            binding.ivDoneMixing->{
                showToast("show frg mix")
            }
        }
    }

    private fun checkCompareDurationMin(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1 > durAudio2
        if (!isCheck) {
            binding.crChangeViewMixing.setDuration(durAudio1)
        } else {
            binding.crChangeViewMixing.setDuration(durAudio2)
        }
    }

    private fun checkCompareDuration(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1 > durAudio2
        if (isCheck) {
            binding.crChangeViewMixing.setDuration(durAudio1)
        } else {
            binding.crChangeViewMixing.setDuration(durAudio2)
        }
    }

    override fun onLineChange(audioFile1: AudioFile, audioFile2: AudioFile, pos: Int) {
        runOnUI {
            mPlayer1.play(audioFile1, pos)
            mPlayer2.play(audioFile2, pos)
        }
    }

    override fun pauseInvalid() {
        mPlayer1.pause()
        mPlayer2.pause()
    }

    override fun changeDuration() {
        mPlayer1.stop()
        mPlayer2.stop()
    }

    override fun setVolumeAudio1(value: Float, min: Float, max: Float) {
        var newValueSound =
            Utils.convertValue(min.toDouble(), max.toDouble(), 0.0, 1.0, value.toDouble())
        Log.d(TAG, "setVolumeAudio1: ${newValueSound.toFloat()}")
        if (newValueSound > 1) {
            newValueSound = 1.0
        }
        if (newValueSound < 0) {
            newValueSound = 0.0
        }
        mPlayer1.setVolume(newValueSound.toFloat())
    }

    override fun setVolumeAudio2(value: Float, min: Float, max: Float) {
        var newValueSound =
            Utils.convertValue(min.toDouble(), max.toDouble(), 0.0, 1.0, value.toDouble())
        Log.d(TAG, "setVolumeAudio2: value $newValueSound")
        if (newValueSound > 1) {
            newValueSound = 1.0
        }
        if (newValueSound < 0) {
            newValueSound = 0.0
        }
        Log.d(TAG, "setVolumeAudio2: editValue ${newValueSound.toFloat()}")
        mPlayer2.setVolume(newValueSound.toFloat())
    }

    override fun endAudioBecauseMaxdistance() {
        mPlayer2.stop()
        mPlayer1.stop()
    }


}
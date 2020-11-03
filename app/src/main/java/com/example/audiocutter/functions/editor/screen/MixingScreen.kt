package com.example.audiocutter.functions.editor.screen


import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.MixingScreenBinding
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.audiochooser.mix.ChangeRangeView
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import com.example.core.core.MixSelector

class MixingScreen : BaseFragment(), View.OnClickListener, ChangeRangeView.OnPlayLineChange {
    private var isCheckClick: Int = 2
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

    private val safeArg: MixingScreenArgs by navArgs()

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

        audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(safeArg.pathAudio1)
        audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(safeArg.pathAudio2)
        durAudio1 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(audioFile1.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
            .toString()

        durAudio2 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(audioFile2.file, MediaMetadataRetriever.METADATA_KEY_DURATION)
            .toString()
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
        binding.shortedTv.setOnClickListener(this)
        binding.longestTv.setOnClickListener(this)
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
            binding.shortedTv -> {
                if (isCheckClick == 2) {
                    changeBackgroundTextView(binding.shortedTv, binding.longestTv)
                    checkCompareDurationMin(durAudio1, durAudio2)
                    isCheckClick = 1
                }
            }
            binding.longestTv -> {
                if (isCheckClick == 1) {
                    changeBackgroundTextView(binding.longestTv, binding.shortedTv)
                    checkCompareDuration(durAudio1, durAudio2)
                    isCheckClick = 2
                }
            }
            binding.ivNextMixing -> {
                binding.crChangeViewMixing.seekNext5S(5000)
            }
            binding.ivPreviousMixing -> {
                binding.crChangeViewMixing.seekPrev5S(5000)
            }
            binding.ivBackMixing -> {
                showToast("back frg")
            }
            binding.ivDoneMixing -> {
                val mixingConfig = AudioMixConfig(
                    "mixing",
                    ManagerFactory.getAudioFileManager().getFolderPath(Folder.TYPE_MIXER),
                    MixSelector.LONGEST,
                    100,
                    100,
                    AudioFormat.MP3
                )



                viewStateManager.editorSaveMixingAudio(this, audioFile1, audioFile2, mixingConfig)
            }
        }
    }

    private fun changeBackgroundTextView(tv1: TextView, tv2: TextView) {
        tv1.setBackgroundResource(R.drawable.bg_next_audio_enabled)
        tv1.setTextColor(resources.getColor(R.color.colorWhite))
        tv2.setBackgroundResource(R.drawable.bg_next_audio_disabled)
        tv2.setTextColor(resources.getColor(R.color.colorgray))
    }

    private fun checkCompareDurationMin(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1.toInt() > durAudio2.toInt()
        if (!isCheck) {
            binding.crChangeViewMixing.setDuration(durAudio1)
        } else {
            binding.crChangeViewMixing.setDuration(durAudio2)
        }
    }

    private fun checkCompareDuration(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1.toInt() > durAudio2.toInt()
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
        Log.d("1010", "setVolumeAudio1: ${newValueSound.toFloat()}")
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
        Log.d("1010", "setVolumeAudio2: value $newValueSound")
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
        mPlayer1.stop()
        mPlayer2.stop()
    }
}
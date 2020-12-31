package com.example.audiocutter.functions.editor.screen


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.AudioPlayer
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.MixingScreenBinding
import com.example.audiocutter.functions.editor.dialogs.FileNameDialogListener
import com.example.audiocutter.functions.editor.dialogs.MixerDialog
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.dialog.CancelDialog
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.audiochooser.mix.ChangeRangeView
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMixConfig
import com.example.core.core.MixSelector

class MixingScreen : BaseFragment(), View.OnClickListener, ChangeRangeView.OnPlayLineChange,
    FileNameDialogListener {
    private var isLongestAudioChecked = true
    private val mPlayer1 = ManagerFactory.newAudioPlayer()
    private val mPlayer2 = ManagerFactory.newAudioPlayer()
    private var durAudio2: String = ""
    private var durAudio1: String = ""
    private val TAG = MixingScreen::class.java.name
    private var playerState = PlayerState.IDLE
    private lateinit var binding: MixingScreenBinding
    private var audioFile1: AudioFile? = null
    private var audioFile2: AudioFile? = null
    private var isCompare = false
    private var dialog: MixerDialog? = null
    private var isDeleteClicked = true
    private var audioFormat: AudioFormat = AudioFormat.MP3

    private val safeArg: MixingScreenArgs by navArgs()

    private val listData = mutableListOf<AudioFile>()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        mPlayer1.init(requireContext())
        mPlayer2.init(requireContext())
        super.onPostCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.mixing_screen, container, false)
        initViews()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chooseObjectObserver()?.getPlayerInfo()?.observe(viewLifecycleOwner, observerAudio())

    }


    private fun observerAudio(): Observer<PlayerInfo> {
        return Observer {
            when (it.playerState) {
                PlayerState.IDLE -> {
                        binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                        playerState = PlayerState.IDLE
                }
                PlayerState.PREPARING -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                }
                PlayerState.PLAYING -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_pause_ic)
                    binding.crChangeViewMixing.setPosition(it.posision)
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
        audioFile1 = ManagerFactory.getAudioFileManager().findAudioFile(safeArg.pathAudio1)
        audioFile2 = ManagerFactory.getAudioFileManager().findAudioFile(safeArg.pathAudio2)

        audioFile1?.let { audio1 ->
            audioFile2?.let { audio2 ->
                durAudio1 = audio1.duration.toString()
                durAudio2 = audio2.duration.toString()
                listData.add(audio1)
                listData.add(audio2)
                binding.crChangeViewMixing.setFileAudio(audio1, audio2)
                isCompare = durAudio1.toInt() > durAudio2.toInt()
                binding.crChangeViewMixing.post{
                    binding.crChangeViewMixing.setLengthAudio(durAudio1, durAudio2)
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
        }
        Log.e(
            TAG,
            "checkFormat: ${getMimeTypeAudio(audioFile1!!.getFilePath())} - ${
                getMimeTypeAudio(audioFile2!!.getFilePath())
            }}"
        )
        if (audioFile1 != null && audioFile2 != null) {
            if (getMimeTypeAudio(audioFile1!!.getFilePath()) == getMimeTypeAudio(audioFile2!!.getFilePath())) {
                audioFormat = if (getMimeTypeAudio(audioFile1!!.getFilePath()) == Constance.MP3) {
                    AudioFormat.MP3
                } else
                    if (getMimeTypeAudio(audioFile1!!.getFilePath()) == Constance.M4A || getMimeTypeAudio(audioFile1!!.getFilePath()) == Constance.AAC) {
                        AudioFormat.AAC
                    } else {
                        AudioFormat.MP3
                    }
            }
        } else {
            audioFormat = if (audioFile1!!.duration > audioFile2!!.duration) {
                getFormatFile(getMimeTypeAudio(audioFile1!!.getFilePath()))
            } else {
                getFormatFile(getMimeTypeAudio(audioFile2!!.getFilePath()))
            }
        }
    }

    private fun getMimeTypeAudio(path: String): String {
        if (path.indexOf(".") != -1) {
            return path.substring(path.lastIndexOf("."), path.length)
        }
        return ""
    }

    private fun getFormatFile(mimeType: String?): AudioFormat {
        val result: AudioFormat = AudioFormat.MP3
        mimeType?.let {
            return if (mimeType == Constance.MP3) {
                AudioFormat.MP3
            } else if (mimeType == Constance.AAC || mimeType == Constance.M4A) {
                AudioFormat.AAC
            } else {
                AudioFormat.MP3
            }
        }
        return result
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (audioFile1 == null || audioFile2 == null) {
            Toast.makeText(requireContext(), R.string.audio_file_is_not_found, Toast.LENGTH_SHORT)
                .show()
            requireActivity().onBackPressed()
        }
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
                            if (audioFile1 != null && audioFile2 != null) {
                                mPlayer1.play(audioFile1!!)
                                mPlayer2.play(audioFile2!!)
                            }

                        } else {
                            mPlayer2.resume()
                            mPlayer1.resume()
                        }
                    }
                }
            }
            binding.shortedTv -> {
                if (isLongestAudioChecked) {
                    changeBackgroundTextView(binding.shortedTv, binding.longestTv)
                    checkCompareDurationMin(durAudio1, durAudio2)
                    binding.crChangeViewMixing.setShortedLength()
                    stopAudio()
                    isLongestAudioChecked = false
                }
            }
            binding.longestTv -> {
                if (!isLongestAudioChecked) {
                    changeBackgroundTextView(binding.longestTv, binding.shortedTv)
                    checkCompareDurationMax(durAudio1, durAudio2)
                    binding.crChangeViewMixing.setLonggestLenght()
                    stopAudio()
                    isLongestAudioChecked = true
                }
            }
            binding.ivNextMixing -> {

                binding.crChangeViewMixing.seekNext5S(5000)
            }
            binding.ivPreviousMixing -> {
                binding.crChangeViewMixing.seekPrev5S(5000)
            }
            binding.ivBackMixing -> {
                activity?.onBackPressed()
            }
            binding.ivDoneMixing -> {
                Log.e(TAG, "checkFormat: ${audioFormat.name}")
                if (isDeleteClicked) {
                    audioFile1?.let {
                        val dialog = MixerDialog.newInstance(this, Utils.getBaseName(it.file))
                        dialog.show(childFragmentManager, CancelDialog.TAG)
                    }
                    isDeleteClicked = false
                }
            }
        }
    }

    private fun chooseObjectObserver(): AudioPlayer? {
        val audioFile1 = this.audioFile1
        val audioFile2 = this.audioFile2
        if (audioFile1 == null || audioFile2 == null) {
            return null
        }
        val longestAudioPlayer =
            if (audioFile1.duration >= audioFile2.duration) mPlayer1 else mPlayer2
        val shortestAudioPlayer =
            if (audioFile1.duration < audioFile2.duration) mPlayer1 else mPlayer2
        return if (isLongestAudioChecked) {
            longestAudioPlayer
        } else {
            shortestAudioPlayer
        }
    }

    private fun stopAudio() {
        mPlayer1.stop()
        mPlayer2.stop()
    }

    private fun changeBackgroundTextView(tv1: TextView, tv2: TextView) {
        tv1.setBackgroundResource(R.drawable.bg_next_audio_enabled)
        tv1.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        tv2.setBackgroundResource(R.drawable.bg_next_audio_disabled)
        tv2.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
    }

    private fun checkCompareDurationMin(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1.toInt() > durAudio2.toInt()
        if (!isCheck) {
            binding.crChangeViewMixing.setDuration(durAudio1)
        } else {
            binding.crChangeViewMixing.setDuration(durAudio2)
        }
    }

    private fun checkCompareDurationMax(durAudio1: String, durAudio2: String) {
        val isCheck = durAudio1.toInt() > durAudio2.toInt()
        if (isCheck) {
            binding.crChangeViewMixing.setDuration(durAudio1)
        } else {
            binding.crChangeViewMixing.setDuration(durAudio2)
        }
    }

    override fun onLineChange(audioFile1: AudioFile, audioFile2: AudioFile, pos: Int) {

        runOnUI {
            if (!chooseObjectObserver()?.getAudioIsPlaying()!!) {
                mPlayer1.play(audioFile1, pos)
                mPlayer2.play(audioFile2, pos)
            } else {
                mPlayer1.seek(pos)
                mPlayer2.seek(pos)
            }

        }
    }

    override fun pauseInvalid() {
        mPlayer1.pause()
        mPlayer2.pause()
    }

    override fun changeDuration() {
        stopAudio()
    }

    override fun setVolumeAudio1(value: Float, min: Float, max: Float) {
        var newValueSound =
            Utils.convertValue(min.toDouble(), max.toDouble(), 0.0, 1.0, value.toDouble())
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
        if (newValueSound > 1) {
            newValueSound = 1.0
        }
        if (newValueSound < 0) {
            newValueSound = 0.0
        }
        mPlayer2.setVolume(newValueSound.toFloat())
    }

    override fun endAudioAtMaxdistance() {
        stopAudio()
    }


    override fun onMixClick(fileName: String) {
        val mixingConfig = AudioMixConfig(
            fileName, ManagerFactory.getAudioFileManager()
                .getFolderPath(Folder.TYPE_MIXER), MixSelector.LONGEST, 100, 100, audioFormat
        )
        if (audioFile1 != null && audioFile2 != null) {
            viewStateManager.editorSaveMixingAudio(this, audioFile1!!, audioFile2!!, mixingConfig)
        }

        isDeleteClicked = true
    }

    override fun onCancel() {
        isDeleteClicked = true
    }
}


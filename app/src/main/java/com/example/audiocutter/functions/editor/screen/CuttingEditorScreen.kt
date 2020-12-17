package com.example.audiocutter.functions.editor.screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.CuttingEditorScreenBinding
import com.example.audiocutter.functions.editor.dialogs.DialogAdvanced
import com.example.audiocutter.functions.editor.dialogs.DialogConvert
import com.example.audiocutter.functions.editor.dialogs.OnDialogAdvanceListener
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioCutConfig
import com.example.core.core.Effect
import com.example.waveform.views.WaveformView1
import com.example.waveform.views.WaveformViewListener
import kotlinx.coroutines.launch
import java.io.File

class CuttingEditorScreen : BaseFragment(), WaveformViewListener,
    View.OnClickListener, View.OnLongClickListener, OnDialogAdvanceListener,
    DialogConvert.OnDialogConvertListener {

    val safeArg: CuttingEditorScreenArgs by navArgs()
    private var playerState = PlayerState.IDLE

    private var fadeIn = Effect.OFF
    private var fadeOut = Effect.OFF

    private var ratioVolumeFadeIn = 0F
    private var ratioVolumeFadeout = 0F
    private lateinit var cuttingViewModel: CuttingViewModel
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {}

    private lateinit var mWaveformView: WaveformView1

    private lateinit var binding: CuttingEditorScreenBinding
    private lateinit var audioPath: String

    companion object {
        private const val TAG = "AudioCutFragment"
        const val RESET_AUDIO_VALUE = 1
        fun newInstance(pathAudio: String): CuttingEditorScreen {
            val args = Bundle()
            args.putString(Utils.KEY_SEND_PATH, pathAudio)
            val fragment = CuttingEditorScreen()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        cuttingViewModel = ViewModelProvider(this).get(CuttingViewModel::class.java)
        audioPath = safeArg.pathAudio
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.cutting_editor_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClick()
        initView()
        initSharePre()
    }

    private fun initSharePre() {
        var fadeInPos = PreferencesHelper.getInt(PreferencesHelper.FADE_IN_TIME, 0)
        var fadeOutPos = PreferencesHelper.getInt(PreferencesHelper.FADE_OUT_TIME, 0)

        fadeIn = Effect.values()[fadeInPos]
        fadeOut = Effect.values()[fadeOutPos]

        ratioVolume()
    }

    override fun onPause() {
        super.onPause()
        cuttingViewModel.pauseAudio()
    }

    private fun initView() {
        mWaveformView = binding.waveEditView
        cuttingViewModel.getAudioPlayerInfo().observe(viewLifecycleOwner, observerAudio())
        cuttingViewModel.loading(audioPath)
            .observe(viewLifecycleOwner, Observer<AudioFile?> {
                if (it == null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.audio_file_is_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    mWaveformView.setWaveformViewListener(this@CuttingEditorScreen)
                    lifecycleScope.launch {
                        mWaveformView.setDataSource(audioPath, it.duration)
                    }
                }
            })
    }

    private fun observerAudio(): Observer<PlayerInfo> {
        return Observer {
            when (it.playerState) {
                PlayerState.IDLE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.IDLE
                    Log.e(TAG, "observerAudio: IDLE")
                }
                PlayerState.PREPARING -> {
                    Log.e(TAG, "observerAudio: PREPARING")
                }
                PlayerState.PLAYING -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_pause_ic)
                    playerState = PlayerState.PLAYING
                    binding.waveEditView.updatePlaybackInMs(it.posision)
                    cuttingViewModel.changeCurrPos(it.posision, false)
                }
                PlayerState.PAUSE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.PAUSE
                }
            }

            if (it.posision <= (fadeIn.time * 1000)) {
                if (fadeIn != Effect.OFF) {
                    cuttingViewModel.setVolume((it.posision.toFloat() / 1000) * ratioVolumeFadeIn)
                }
            } else if (it.posision < cuttingViewModel.getCuttingEndPos() - fadeOut.time * 1000) {
                cuttingViewModel.setVolume(1f)
            } else if (it.posision >= cuttingViewModel.getCuttingEndPos() - (fadeOut.time * 1000)) {
                if (fadeOut != Effect.OFF) {
                    cuttingViewModel.setVolume(
                        ((cuttingViewModel.getCuttingEndPos()
                            .toFloat() - it.posision.toFloat()) / 1000) * ratioVolumeFadeout
                    )
                }
            }
        }
    }

    private fun setClick() {
        binding.optionIv.setOnClickListener(this)
        binding.tickIv.setOnClickListener(this)
        binding.increaseStartTimeIv.setOnClickListener(this)
        binding.reductionStartTimeIv.setOnClickListener(this)
        binding.increaseEndTimeIv.setOnClickListener(this)
        binding.reductionEndTimeIv.setOnClickListener(this)
        binding.zoomOutIv.setOnClickListener(this)
        binding.zoomInIv.setOnClickListener(this)
        binding.preIv.setOnClickListener(this)
        binding.nextIv.setOnClickListener(this)
        binding.playRl.setOnClickListener(this)
        binding.closeIv.setOnClickListener(this)

        binding.increaseStartTimeIv.setOnLongClickListener(this)
        binding.reductionStartTimeIv.setOnLongClickListener(this)
        binding.increaseEndTimeIv.setOnLongClickListener(this)
        binding.reductionEndTimeIv.setOnLongClickListener(this)

    }


    override fun onStartTimeChanged(startTimeMs: Long) {
        val startTimeStr = Utils.longDurationMsToStringMs(startTimeMs)
        val textWidth = binding.startTimeTv.paint.measureText(startTimeStr)
        if (binding.startTimeTv.width < textWidth) {
            val layoutParams = binding.startTimeTv.layoutParams
            layoutParams.width = binding.startTimeTv.paint.measureText("a${startTimeStr}").toInt()
            binding.startTimeTv.gravity = Gravity.CENTER_HORIZONTAL
            binding.startTimeTv.layoutParams = layoutParams
        }
        binding.startTimeTv.text = startTimeStr
        cuttingViewModel.changeStartPos(startTimeMs.toInt())
    }

    override fun onEndTimeChanged(endTimeMs: Long) {
        val endTimeStr = Utils.longDurationMsToStringMs(endTimeMs)
        val textWidth = binding.endTimeTv.paint.measureText(endTimeStr)
        if (binding.endTimeTv.width < textWidth) {
            val layoutParams = binding.endTimeTv.layoutParams
            layoutParams.width = binding.endTimeTv.paint.measureText("a${endTimeStr}").toInt()
            binding.endTimeTv.gravity = Gravity.CENTER_HORIZONTAL
            binding.endTimeTv.layoutParams = layoutParams
        }
        binding.endTimeTv.text = Utils.longDurationMsToStringMs(endTimeMs)
        cuttingViewModel.changeEndPos(endTimeMs.toInt())
    }

    override fun onPlayPositionChanged(positionMs: Int, isPress: Boolean) {
        when {
            positionMs in cuttingViewModel.getCuttingStartPos()..cuttingViewModel.getCuttingEndPos() -> {
                if (isPress) {
                    cuttingViewModel.changeCurrPos(positionMs)
                } else {
                    cuttingViewModel.changeCurrPos(positionMs, false)
                }

            }
        }
    }

    override fun onDraggingPlayPos(isFinished: Boolean) {
        cuttingViewModel.pauseAudio()
        if (isFinished) {
            cuttingViewModel.changeCurrPos(mWaveformView.getPlaybackInMs())
        }
    }

    override fun onPlayPosOutOfRange(isEnd:Boolean) {
        if(isEnd){
            cuttingViewModel.currPosReachToEnd()
        }else{
            cuttingViewModel.currPosReachToStart()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (cuttingViewModel.getAudioFile() == null) {
            requireActivity().onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closeIv -> {
                activity?.onBackPressed()
            }
            binding.optionIv -> {
                DialogAdvanced.showDialogAdvanced(requireContext(), this)
                /* ManagerFactory.getAudioPlayer().pause()*/
                cuttingViewModel.pauseAudio()
            }
            binding.tickIv -> {
                cuttingViewModel.getAudioFile()?.let {
                    DialogConvert.showDialogConvert(
                        childFragmentManager,
                        this,
                        Utils.getBaseName(File(audioPath))
                    )
                }
            }
            binding.increaseStartTimeIv -> {
                mWaveformView.setStartTimeMs(mWaveformView.getStartTimeMs() + Utils.TIME_CHANGE)
            }
            binding.reductionStartTimeIv -> {
                mWaveformView.setStartTimeMs(mWaveformView.getStartTimeMs() - Utils.TIME_CHANGE)
            }
            binding.increaseEndTimeIv -> {
                mWaveformView.setEndTimeMs(mWaveformView.getEndTimeMs() + Utils.TIME_CHANGE)
            }
            binding.reductionEndTimeIv -> {
                mWaveformView.setEndTimeMs(mWaveformView.getEndTimeMs() - Utils.TIME_CHANGE)
            }
            binding.zoomOutIv -> {
                binding.waveEditView.zoomOut()
            }
            binding.zoomInIv -> {
                binding.waveEditView.zoomIn()
            }
            binding.preIv -> {
                if (cuttingViewModel.getCuttingCurrPos() - Utils.FIVE_SECOND <= 0) {
                    cuttingViewModel.seekAudio(0)
                } else {
                    cuttingViewModel.seekAudio(cuttingViewModel.getCuttingCurrPos() - Utils.FIVE_SECOND)
                }
            }
            binding.playRl -> {
                runOnUI {
                    cuttingViewModel.clickedPlayButton()
                }
            }
            binding.nextIv -> {
                cuttingViewModel.seekAudio(cuttingViewModel.getCuttingCurrPos() + Utils.FIVE_SECOND)
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            binding.increaseEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = false,
                    view = binding.increaseEndTimeIv
                )
            }
            binding.increaseStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = true,
                    view = binding.increaseStartTimeIv
                )
            }
            binding.reductionStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = true,
                    view = binding.reductionStartTimeIv
                )
            }
            binding.reductionEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = false,
                    view = binding.reductionEndTimeIv
                )
            }
        }
        return true
    }

    //isIncrease -> true increase, false reduction
    //isStart -> true timeStart, false timeEnd
    private fun updateTimeWaveView(isIncrease: Boolean, isStart: Boolean, view: View) {
        mHandler.removeCallbacks(runnable)
        runnable = Runnable {
            if (!view.isPressed) return@Runnable
            if (isStart) {
                mWaveformView.setStartTimeMs(if (isIncrease) mWaveformView.getStartTimeMs() + 100 else mWaveformView.getStartTimeMs() - 100)
            } else {
                mWaveformView.setEndTimeMs(if (isIncrease) mWaveformView.getEndTimeMs() + 100 else mWaveformView.getEndTimeMs() - 100)
            }
            mHandler.postDelayed(runnable, 50)
        }
        mHandler.post(runnable)
    }

    override fun onDialogOk(fadeIn: Effect, fadeOut: Effect) {
        this.fadeIn = fadeIn
        this.fadeOut = fadeOut
        /* ManagerFactory.getAudioPlayer().seek(50)
         ManagerFactory.getAudioPlayer().resume()*/
        cuttingViewModel.seekAudio(RESET_AUDIO_VALUE)
        cuttingViewModel.resumeAudio()
        ratioVolume()
    }

    private fun ratioVolume() {
        ratioVolumeFadeIn = if (fadeIn != Effect.OFF) (1F / this.fadeIn.time) else 0F
        ratioVolumeFadeout = if (fadeOut != Effect.OFF) (1F / this.fadeOut.time) else 0F
    }

    override fun onAcceptConvert(audioCutConfig: AudioCutConfig) {
        var audioConfig = audioCutConfig
        audioConfig.inEffect = fadeIn
        audioConfig.outEffect = fadeOut
        audioConfig.startPosition = cuttingViewModel.getCuttingStartPos().toFloat() / 1000

        audioConfig.endPosition = (cuttingViewModel.getCuttingEndPos()
            .toFloat() / 1000) - audioConfig.startPosition

        audioConfig.pathFolder = cuttingViewModel.getStorageFolder()

        cuttingViewModel.getAudioFile()?.let {
            viewStateManager.editorSaveCutingAudio(this, it, audioCutConfig)
        }


    }

}
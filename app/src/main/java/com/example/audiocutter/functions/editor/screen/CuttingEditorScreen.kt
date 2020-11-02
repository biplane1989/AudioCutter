package com.example.audiocutter.functions.editor.screen

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.CuttingEditorScreenBinding
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.functions.editor.dialogs.DialogAdvanced
import com.example.audiocutter.functions.editor.dialogs.DialogConvert
import com.example.audiocutter.functions.editor.dialogs.OnDialogAdvanceListener
import com.example.audiocutter.ui.editor.cutting.WaveformEditView
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioCore
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.Effect
import java.io.File

class CuttingEditorScreen : BaseFragment(), WaveformEditView.WaveformEditListener,
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

    private lateinit var mEditView: WaveformEditView

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
        cuttingViewModel.restore(audioPath)
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
        mEditView = binding.waveEditView

        binding.waveEditView.apply {
            setListener(this@CuttingEditorScreen)
            setDataSource(audioPath)
        }
        cuttingViewModel.getAudioPlayerInfo().observe(viewLifecycleOwner, observerAudio())
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
            binding.waveEditView.setPlayPositionMs(it.posision, false)
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
        if (cuttingViewModel.getCuttingCurrPos() < startTimeMs) {
            mEditView.setPlayPositionMs(startTimeMs.toInt(), true)
        }
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
        if (cuttingViewModel.getCuttingCurrPos() >= cuttingViewModel.getCuttingEndPos()) {
            mEditView.setPlayPositionMs(cuttingViewModel.getCuttingEndPos().toInt(), true)
        }
    }

    override fun onPlayPositionChanged(positionMs: Int, isPress: Boolean) {
        when {
            positionMs in cuttingViewModel.getCuttingStartPos()..cuttingViewModel.getCuttingEndPos() -> {
                /* if (isPress && playerState == PlayerState.PLAYING) {
                     if (positionMs.toLong() == endPos) {
                         ManagerFactory.getAudioPlayer().stop()
                         playPos = startPos.toInt()
                         mEditView.setPlayPositionMs(if (playPos == 0) 50 else playPos, false)
                     } else {
                         ManagerFactory.getAudioPlayer().seek(positionMs)
                         playPos = positionMs
                     }
                 } else {
                     if (playerState != PlayerState.PLAYING) {
                         ManagerFactory.getAudioPlayer().seek(positionMs)
                     }
                     playPos = positionMs
                 }*/
                if (isPress) {
                    cuttingViewModel.changeCurrPos(positionMs)
                } else {
                    cuttingViewModel.changeCurrPos(positionMs, false)
                }

                if (isPress && positionMs >= cuttingViewModel.getCuttingEndPos()) {
                    mEditView.setPlayPositionMs(
                        if (cuttingViewModel.getCuttingCurrPos() == 0) RESET_AUDIO_VALUE else cuttingViewModel.getCuttingCurrPos(),
                        false
                    )
                }

            }
            positionMs < cuttingViewModel.getCuttingStartPos() -> {
                mEditView.setPlayPositionMs(cuttingViewModel.getCuttingStartPos().toInt(), true)
            }
            positionMs > cuttingViewModel.getCuttingEndPos() -> {
                mEditView.setPlayPositionMs(cuttingViewModel.getCuttingEndPos().toInt(), true)
            }
        }
    }

    override fun onCountAudioSelected(positionMs: Long, isFirstTime: Boolean) {
        val longDurationMsToStringMs = Utils.longDurationMsToStringMs(positionMs)
        binding.timeAudioTv.text = longDurationMsToStringMs
        if (isFirstTime) binding.endTimeTv.text = longDurationMsToStringMs
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
                DialogConvert.showDialogConvert(
                    childFragmentManager,
                    this,
                    cuttingViewModel.getAudioFile(),
                    cuttingViewModel.getNameSuggestion()
                )
            }
            binding.increaseStartTimeIv -> {
                mEditView.setStartTimeMs(mEditView.getTimeStart() + Utils.TIME_CHANGE)
            }
            binding.reductionStartTimeIv -> {
                mEditView.setStartTimeMs(mEditView.getTimeStart() - Utils.TIME_CHANGE)
            }
            binding.increaseEndTimeIv -> {
                mEditView.setEndTimeMs(mEditView.getTimeEnd() + Utils.TIME_CHANGE)
            }
            binding.reductionEndTimeIv -> {
                mEditView.setEndTimeMs(mEditView.getTimeEnd() - Utils.TIME_CHANGE)
            }
            binding.zoomOutIv -> {
                binding.waveEditView.zoomOut()
            }
            binding.zoomInIv -> {
                binding.waveEditView.zoomInt()
            }
            binding.preIv -> {
                mEditView.setPlayPositionMs(
                    if ((cuttingViewModel.getCuttingCurrPos() - Utils.FIVE_SECOND) <= 0) RESET_AUDIO_VALUE else cuttingViewModel.getCuttingCurrPos() - Utils.FIVE_SECOND,
                    true
                )
            }
            binding.playRl -> {
                runOnUI {
                    cuttingViewModel.clickedPlayButton()
                }
            }
            binding.nextIv -> {
                mEditView.setPlayPositionMs(
                    cuttingViewModel.getCuttingCurrPos() + Utils.FIVE_SECOND,
                    true
                )
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
                mEditView.setStartTimeMs(if (isIncrease) mEditView.getTimeStart() + 100 else mEditView.getTimeStart() - 100)
            } else {
                mEditView.setEndTimeMs(if (isIncrease) mEditView.getTimeEnd() + 100 else mEditView.getTimeEnd() - 100)
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


        viewStateManager.editorSaveCutingAudio(this, cuttingViewModel.getAudioFile(), audioCutConfig)

    }

}
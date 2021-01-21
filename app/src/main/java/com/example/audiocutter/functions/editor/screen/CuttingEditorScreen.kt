package com.example.audiocutter.functions.editor.screen

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
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
import com.example.waveform.views.WaveformView
import com.example.waveform.views.WaveformViewListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File


class CuttingEditorScreen : BaseFragment(), WaveformViewListener, View.OnClickListener, View.OnLongClickListener, OnDialogAdvanceListener, DialogConvert.OnDialogConvertListener, TextView.OnEditorActionListener, View.OnFocusChangeListener {

    val safeArg: CuttingEditorScreenArgs by navArgs()
    private var playerState = PlayerState.IDLE

    private var fadeIn = Effect.OFF
    private var fadeOut = Effect.OFF

    private var ratioVolumeFadeIn = 0F
    private var ratioVolumeFadeout = 0F
    private lateinit var cuttingViewModel: CuttingViewModel
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {}
    private var duration = 0L
    private var startTimeOld = "00:00.0"
    private var endTimeOld = "00:00.0"
    private var startTimeLife = 0L
    private var endTimeLife = 0L
//    private var isShowAbsStart = true
//    private var isShowAbsEnd = true

    private lateinit var mWaveformView: WaveformView

    private lateinit var binding: CuttingEditorScreenBinding
    private lateinit var audioPath: String

    companion object {
        //        val HOUR_FORMAT = "HOUR_FORMAT"
        val MINUTE_FORMAT = "MINUTE_FORMAT"
        val SECOND_FORMAT = "SECOND_FORMAT"
        val START_KEY = "START_KEY"
        val END_KEY = "END_KEY"

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

        binding.startTimeTv.setKeyImeChangeListener(object : TimeEditText.KeyImeChange {        // su kien back tren ban phim
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    binding.startTimeTv.clearFocus()
                    val startTime = checkValidationTime(binding.startTimeTv.text.toString(), START_KEY)
                    if (startTime == null) {
                        val editable: Editable = SpannableStringBuilder(startTimeOld)
                        binding.startTimeTv.text = editable
                    } else {
                        binding.startTimeTv.text = startTime
                        startTimeOld = startTime.toString()
                        mWaveformView.setStartTimeMs(startTimeLife.toInt())
                    }
                }
            }
        })

        binding.endTimeTv.setKeyImeChangeListener(object : TimeEditText.KeyImeChange {      // su kien back tren ban phim
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    binding.endTimeTv.clearFocus()
                    val endTime = checkValidationTime(binding.endTimeTv.text.toString(), END_KEY)
                    if (endTime == null) {
                        val editable: Editable = SpannableStringBuilder(endTimeOld)
                        binding.endTimeTv.text = editable
                    } else {
                        binding.endTimeTv.text = endTime
                        endTimeOld = endTime.toString()
                        mWaveformView.setEndTimeMs(endTimeLife.toInt())
                    }
                }
            }
        })

        binding.startTimeTv.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    val positionCount = binding.startTimeTv.selectionStart
                    Log.d(TAG, "onKey: value : ${binding.endTimeTv.text.get(positionCount - 1)}")
                    if (positionCount >= 1) {
                        if (TextUtils.equals(binding.startTimeTv.text.toString()
                                .get(positionCount - 1).toString(), ":")) return true
                        if (TextUtils.equals(binding.startTimeTv.text.toString()
                                .get(positionCount - 1).toString(), ".")) return true
                        return false
                    }
                    return true
                }
                return false
            }
        })
        binding.endTimeTv.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    val positionCount = binding.endTimeTv.selectionStart

                    if (positionCount >= 1) {
                        if (TextUtils.equals(binding.endTimeTv.text.toString()
                                .get(positionCount - 1).toString(), ":")) return true
                        if (TextUtils.equals(binding.endTimeTv.text.toString()
                                .get(positionCount - 1).toString(), ".")) return true
                        return false
                    }
                    return true
                }
                return false
            }
        })
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onResume() {       // xu ly back button
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                requireActivity().onBackPressed()
                true
            } else false
        }
    }

    private fun checkValidationTime(time: String, key: String): Editable? {
        if (duration <= 0) {
            showNotification(getString(R.string.cutting_editor_screen_error_validator))
            return null
        }

        var timePattern = ""
//        if (duration / (3600 * 1000) >= 1) {
//            timePattern = "([0-9]{1,9})+:[0-9]{1,2}+:[0-9]{1,2}+.[0-9]{1}"
//
//        } else {
        timePattern = "[0-9]{1,9}+:[0-9]{1,2}+.[0-9]{1}"
//        }
        if (time.matches(timePattern.toRegex())) {
//            if (duration / (3600 * 1000) >= 1) {         // hour
//                return checkTimeInput(time, key, HOUR_FORMAT)
//            } else {
            if (duration / (60 * 1000) >= 1) {       // minute
                return checkTimeInput(time, key, MINUTE_FORMAT)
            } else {                                 // second
                return checkTimeInput(time, key, SECOND_FORMAT)
            }
//            }
        } else {
            showNotification(getString(R.string.cutting_editor_screen_error_validator))
            return null
        }
    }

    private fun checkTimeInput(time: String, key: String, typeTime: String): Editable? {
        if (key.equals(START_KEY)) {
            startTimeLife = Utils.getTimeByTimeString(time, typeTime)
            val oldTimeEnd = Utils.getTimeByTimeString(endTimeOld, typeTime) - 1000
            if (startTimeLife > -1 && startTimeLife <= duration && startTimeLife < oldTimeEnd) {
                return Utils.formatTime(time, typeTime)
            } else {
                showNotification(getString(R.string.cutting_editor_screen_error_validator))
                return null
            }
        } else {
            endTimeLife = Utils.getTimeByTimeString(time, typeTime)
            val oldTimeNew = Utils.getTimeByTimeString(startTimeOld, typeTime) + 1000
            if (endTimeLife > -1 && endTimeLife <= duration && endTimeLife > oldTimeNew) {
                return Utils.formatTime(time, typeTime)
            } else {
                showNotification(getString(R.string.cutting_editor_screen_error_validator))
                return null
            }
        }
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
        cuttingViewModel.loading(requireContext(), audioPath)
            .observe(viewLifecycleOwner, Observer<AudioFile?> {
                if (it == null) {
                    context?.let {
                        showNotification(getString(R.string.audio_file_is_not_found))
                    }
                } else {
                    mWaveformView.setWaveformViewListener(this@CuttingEditorScreen)
                    lifecycleScope.launch {
                        mWaveformView.setDataSource(audioPath, it.duration)
                        duration = it.duration
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
                    cuttingViewModel.changeCurrPos(cuttingViewModel.getCuttingEndPos(), false)
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

            if ((it.posision - cuttingViewModel.getCuttingStartPos()) <= (fadeIn.time * 1000)) {
                if (fadeIn != Effect.OFF) {
                    cuttingViewModel.setVolume(((it.posision.toFloat()- cuttingViewModel.getCuttingStartPos()) / 1000) * ratioVolumeFadeIn)
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

        binding.startTimeTv.setOnClickListener(this)
        binding.endTimeTv.setOnClickListener(this)
        binding.adsView.setOnClickListener(this)

        binding.startTimeTv.setOnEditorActionListener(this)
        binding.endTimeTv.setOnEditorActionListener(this)
        binding.startTimeTv.setOnFocusChangeListener(this)
        binding.endTimeTv.setOnFocusChangeListener(this)

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

        val editable: Editable = SpannableStringBuilder(startTimeStr)
        binding.startTimeTv.text = editable
        startTimeOld = startTimeStr

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
        val editable: Editable = SpannableStringBuilder(endTimeStr)
        binding.endTimeTv.text = editable
        endTimeOld = endTimeStr
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

    override fun onPlayPosOutOfRange(isEnd: Boolean) {
        if (isEnd) {
            cuttingViewModel.currPosReachToEnd()
        } else {
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
//                cuttingViewModel.resetPlayerInfo()
                activity?.onBackPressed()
            }
            binding.optionIv -> {
//                DialogAdvanced.showDialogAdvanced(requireContext(), this)
                val dialog = DialogAdvanced.newInstance(this)
                dialog.show(childFragmentManager, DialogAdvanced.TAG)
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
            binding.startTimeTv -> {
            }
            binding.endTimeTv -> {
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            binding.increaseEndTimeIv -> {
                updateTimeWaveView(isIncrease = true, isStart = false, view = binding.increaseEndTimeIv)
            }
            binding.increaseStartTimeIv -> {
                updateTimeWaveView(isIncrease = true, isStart = true, view = binding.increaseStartTimeIv)
            }
            binding.reductionStartTimeIv -> {
                updateTimeWaveView(isIncrease = false, isStart = true, view = binding.reductionStartTimeIv)
            }
            binding.reductionEndTimeIv -> {
                updateTimeWaveView(isIncrease = false, isStart = false, view = binding.reductionEndTimeIv)
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
        cuttingViewModel.changeCurrPos(cuttingViewModel.getCuttingEndPos())
        ratioVolume()
        lifecycleScope.launchWhenResumed {
            cuttingViewModel.clickedPlayButton()
        }

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

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onEditorAction(view: TextView?, actionId: Int, p2: KeyEvent?): Boolean {       // xu ly su kien an enter tren ban phim

        if (actionId == EditorInfo.IME_ACTION_DONE && (actionId == KeyEvent.KEYCODE_ENTER)) {
            val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(binding.startTimeTv.getWindowToken(), 0)
            imm.hideSoftInputFromWindow(binding.endTimeTv.getWindowToken(), 0)
            return true
        } else {
//            when (view) {
//                binding.startTimeTv -> {
                    val startTime = checkValidationTime(binding.startTimeTv.text.toString(), START_KEY)
                    if (startTime == null) {
                        val editable: Editable = SpannableStringBuilder(startTimeOld)
                        binding.startTimeTv.text = editable
                    } else {
                        binding.startTimeTv.text = startTime
                        startTimeOld = startTime.toString()
                        Log.d(TAG, "onEditorAction: start : startTimeLife: " + startTimeLife + "endtimeLife: " + endTimeLife)
                        mWaveformView.setStartTimeMs(startTimeLife.toInt())
                    }
                    binding.startTimeTv.clearFocus()
//                    hideKeyboard()
//                }
//                binding.endTimeTv -> {
                    val endTime = checkValidationTime(binding.endTimeTv.text.toString(), END_KEY)
                    if (endTime == null) {
                        val editable: Editable = SpannableStringBuilder(endTimeOld)
                        binding.endTimeTv.text = editable
                    } else {
                        binding.endTimeTv.text = endTime
                        endTimeOld = endTime.toString()
                        mWaveformView.setEndTimeMs(endTimeLife.toInt())
                        Log.d(TAG, "onEditorAction: end : startTimeLife: " + startTimeLife + "endtimeLife: " + endTimeLife)
                    }
                    binding.endTimeTv.clearFocus()
                    hideKeyboard()
//                }
//            }

            return false
        }
    }

    override fun onFocusChange(view: View?, p1: Boolean) {          // su kien thay doi focus EditTime
        view?.let {
            when (view) {
                binding.startTimeTv -> {
//                    isShowAbsStart = !isShowAbsStart
//                    showHideAbs(isShowAbsStart)
                }
                binding.endTimeTv -> {
//                    isShowAbsEnd = !isShowAbsEnd
//                    showHideAbs(isShowAbsEnd)
                }
            }
        }
    }

    private fun showHideAbs(isShowStatus: Boolean) {
        if (isShowStatus) {
            binding.adsView.visibility = View.VISIBLE
        } else {
            binding.adsView.visibility = View.GONE
        }
    }

}
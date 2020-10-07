package com.example.audiocutter.functions.screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.FragmentAudioCutBinding
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.fragment_cut.view.WaveformEditView
import com.example.audiocutter.util.Utils

class AudioCutFragment : BaseFragment(), WaveformEditView.WaveformEditListener,
    View.OnClickListener, View.OnLongClickListener {
    private lateinit var pathAudio: String
    private lateinit var audioFile: AudioFile
    private var playerState = PlayerState.IDLE

    private var playPos = 0
    private var startPos = 0L
    private var endPos = 0L

    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {}

    private lateinit var mEditView: WaveformEditView

    private lateinit var fragmentCutBinding: FragmentAudioCutBinding

    override fun onPostCreate(savedInstanceState: Bundle?) {
        getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentCutBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_audio_cut, container, false)
        return fragmentCutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClick()
        initView()
    }

    private fun initView() {
        mEditView = fragmentCutBinding.waveEditView

        fragmentCutBinding.waveEditView.apply {
            setListener(this@AudioCutFragment)
            setDataSource(pathAudio)
        }
        var layoutParams1 = LinearLayout.LayoutParams(
            Utils.getWidthText(context = requireContext()).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams1.gravity = Gravity.CENTER
        fragmentCutBinding.startTimeTv.apply { layoutParams = layoutParams1 }
        fragmentCutBinding.endTimeTv.apply { layoutParams = layoutParams1 }

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(viewLifecycleOwner, observerAudio())
    }

    private fun observerAudio(): Observer<PlayerInfo> {
        return Observer {
            when (it.playerState) {
                PlayerState.IDLE -> {
                    fragmentCutBinding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.IDLE
                    Log.e(TAG, "observerAudio: IDLE")
                }
                PlayerState.PREPARING -> {
                    Log.e(TAG, "observerAudio: PREPARING")
                }
                PlayerState.PLAYING -> {
                    fragmentCutBinding.playIv.setImageResource(R.drawable.fragment_cutter_pause_ic)
                    playerState = PlayerState.PLAYING
                }
                PlayerState.PAUSE -> {
                    fragmentCutBinding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.PAUSE
                }
            }
            fragmentCutBinding.waveEditView.setPlayPositionMs(it.posision, false)
        }
    }

    private fun setClick() {
        fragmentCutBinding.optionIv.setOnClickListener(this)
        fragmentCutBinding.tickIv.setOnClickListener(this)
        fragmentCutBinding.increaseStartTimeIv.setOnClickListener(this)
        fragmentCutBinding.reductionStartTimeIv.setOnClickListener(this)
        fragmentCutBinding.increaseEndTimeIv.setOnClickListener(this)
        fragmentCutBinding.reductionEndTimeIv.setOnClickListener(this)
        fragmentCutBinding.zoomOutIv.setOnClickListener(this)
        fragmentCutBinding.zoomInIv.setOnClickListener(this)
        fragmentCutBinding.preIv.setOnClickListener(this)
        fragmentCutBinding.nextIv.setOnClickListener(this)
        fragmentCutBinding.playRl.setOnClickListener(this)

        fragmentCutBinding.increaseStartTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.reductionStartTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.increaseEndTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.reductionEndTimeIv.setOnLongClickListener(this)

    }


    private fun getData() {
        pathAudio = requireArguments().getString(Utils.KEY_SEND_PATH, null)
        audioFile = ManagerFactory.getAudioFileManagerImpl().buildAudioFile(pathAudio)
    }

    companion object {
        private const val TAG = "AudioCutFragment"

        fun newInstance(pathAudio: String): AudioCutFragment {
            val args = Bundle()
            args.putSerializable(Utils.KEY_SEND_PATH, pathAudio)
            val fragment = AudioCutFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStartTimeChanged(startTimeMs: Long) {
        fragmentCutBinding.startTimeTv.text =
            Utils.longDurationMsToStringMs(startTimeMs)
        startPos = startTimeMs
        if (playPos <= startTimeMs) {
            mEditView.setPlayPositionMs(startTimeMs.toInt(), true)
        }
    }

    override fun onEndTimeChanged(endTimeMs: Long) {
        fragmentCutBinding.endTimeTv.text = Utils.longDurationMsToStringMs(endTimeMs)
        endPos = endTimeMs
        if (playPos >= endPos) {
            mEditView.setPlayPositionMs(endPos.toInt(), true)
        }
    }

    override fun onPlayPositionChanged(positionMs: Int, isPress: Boolean) {
        when {
            positionMs in startPos..endPos -> {
                Log.e(TAG, "onPlayPositionChanged: $isPress")
                if (isPress && playerState == PlayerState.PLAYING) {
                    if (positionMs.toLong() == endPos) {
                        ManagerFactory.getAudioPlayer().stop()
                        playPos = startPos.toInt()
                        mEditView.setPlayPositionMs(playPos, false)
                    } else {
                        ManagerFactory.getAudioPlayer().seek(positionMs)
                        playPos = positionMs
                    }
                }
            }
            positionMs < startPos -> {
                mEditView.setPlayPositionMs(startPos.toInt(), true)
            }
            positionMs > endPos -> {
                mEditView.setPlayPositionMs(endPos.toInt(), true)
            }
        }
    }

    override fun onCountAudioSelected(positionMs: Long, isFirstTime: Boolean) {
        val longDurationMsToStringMs = Utils.longDurationMsToStringMs(positionMs)
        fragmentCutBinding.timeAudioTv.text =
            longDurationMsToStringMs
        if (isFirstTime)
            fragmentCutBinding.endTimeTv.text = longDurationMsToStringMs
    }

    override fun onClick(v: View?) {
        when (v) {
            fragmentCutBinding.closeIv -> {

            }
            fragmentCutBinding.optionIv -> {

            }
            fragmentCutBinding.tickIv -> {

            }
            fragmentCutBinding.increaseStartTimeIv -> {
                mEditView.setStartTimeMs(mEditView.getTimeStart() + 100)
            }
            fragmentCutBinding.reductionStartTimeIv -> {
                mEditView.setStartTimeMs(mEditView.getTimeStart() - 100)
            }
            fragmentCutBinding.increaseEndTimeIv -> {
                mEditView.setEndTimeMs(mEditView.getTimeEnd() + 100)
            }
            fragmentCutBinding.reductionEndTimeIv -> {
                mEditView.setEndTimeMs(mEditView.getTimeEnd() - 100)
            }
            fragmentCutBinding.zoomOutIv -> {
                fragmentCutBinding.waveEditView.zoomOut()
            }
            fragmentCutBinding.zoomInIv -> {
                fragmentCutBinding.waveEditView.zoomInt()
            }
            fragmentCutBinding.preIv -> {

            }
            fragmentCutBinding.playRl -> {
                runOnUI {
                    if (playerState == PlayerState.PLAYING) {
                        ManagerFactory.getAudioPlayer().pause()
                    } else {
                        if (playerState == PlayerState.IDLE)
                            ManagerFactory.getAudioPlayer().play(audioFile, playPos)
                        else ManagerFactory.getAudioPlayer().resume()
                    }
                }
            }
            fragmentCutBinding.nextIv -> {

            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            fragmentCutBinding.increaseEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = false,
                    view = fragmentCutBinding.increaseEndTimeIv
                )
            }
            fragmentCutBinding.increaseStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = true,
                    view = fragmentCutBinding.increaseStartTimeIv
                )
            }
            fragmentCutBinding.reductionStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = true,
                    view = fragmentCutBinding.reductionStartTimeIv
                )
            }
            fragmentCutBinding.reductionEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = false,
                    view = fragmentCutBinding.reductionEndTimeIv
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

}
package com.example.audiocutter.functions.fragmentcutterscreen

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
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FragmentAudioCutBinding
import com.example.audiocutter.ui.fragment_cut.WaveformEditView
import com.example.audiocutter.util.Utils

class AudioCutFragment : BaseFragment(), WaveformEditView.WaveformEditListener,
    View.OnClickListener, View.OnLongClickListener {
    private lateinit var pathAudio: String
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {}

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
        fragmentCutBinding.fragmentCutterWaveEditView.apply {
            setListener(this@AudioCutFragment)
            setDataSource(pathAudio)
        }
        var layoutParams1 = LinearLayout.LayoutParams(
            Utils.getWidthText(context = requireContext()).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams1.gravity = Gravity.CENTER
        fragmentCutBinding.fragmentCutterStartTimeTv.apply { layoutParams = layoutParams1 }
    }

    private fun setClick() {
        fragmentCutBinding.fragmentCutterOptionIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterTickIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterIncreaseStartTimeIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterReductionStartTimeIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterIncreaseEndTimeIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterReductionEndTimeIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterZoomOutIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterZoomInIv.setOnClickListener(this)

        fragmentCutBinding.fragmentCutterIncreaseStartTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.fragmentCutterReductionStartTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.fragmentCutterIncreaseEndTimeIv.setOnLongClickListener(this)
        fragmentCutBinding.fragmentCutterReductionEndTimeIv.setOnLongClickListener(this)

    }


    private fun getData() {
        pathAudio = requireArguments().getString(Utils.KEY_SEND_PATH, null)
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
        fragmentCutBinding.fragmentCutterStartTimeTv.text =
            Utils.longDurationMsToStringMs(startTimeMs)
    }

    override fun onEndTimeChanged(endTimeMs: Long) {
        fragmentCutBinding.fragmentCutterEndTimeTv.text = Utils.longDurationMsToStringMs(endTimeMs)
    }

    override fun onPlayPositionChanged(positionMs: Int) {
        Log.e(TAG, "onPlayPositionChanged: $positionMs")
    }

    override fun onCountAudioSelected(positionMs: Long, isFirstTime: Boolean) {
        val longDurationMsToStringMs = Utils.longDurationMsToStringMs(positionMs)
        fragmentCutBinding.fragmentCutterTimeAudioTv.text =
            longDurationMsToStringMs
        if (isFirstTime)
            fragmentCutBinding.fragmentCutterEndTimeTv.text = longDurationMsToStringMs
    }

    override fun onClick(v: View?) {
        when (v) {
            fragmentCutBinding.fragmentCutterCloseIv -> {

            }
            fragmentCutBinding.fragmentCutterOptionIv -> {

            }
            fragmentCutBinding.fragmentCutterTickIv -> {

            }
            fragmentCutBinding.fragmentCutterIncreaseStartTimeIv -> {
            }
            fragmentCutBinding.fragmentCutterReductionStartTimeIv -> {
            }
            fragmentCutBinding.fragmentCutterIncreaseEndTimeIv -> {
            }
            fragmentCutBinding.fragmentCutterReductionEndTimeIv -> {
            }
            fragmentCutBinding.fragmentCutterZoomOutIv -> {
                fragmentCutBinding.fragmentCutterWaveEditView.zoomOut()
            }
            fragmentCutBinding.fragmentCutterZoomInIv -> {
                fragmentCutBinding.fragmentCutterWaveEditView.zoomInt()
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            fragmentCutBinding.fragmentCutterIncreaseEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = false,
                    view = fragmentCutBinding.fragmentCutterIncreaseEndTimeIv
                )
            }
            fragmentCutBinding.fragmentCutterIncreaseStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = true,
                    isStart = true,
                    view = fragmentCutBinding.fragmentCutterIncreaseStartTimeIv
                )
            }
            fragmentCutBinding.fragmentCutterReductionStartTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = true,
                    view = fragmentCutBinding.fragmentCutterReductionStartTimeIv
                )
            }
            fragmentCutBinding.fragmentCutterReductionEndTimeIv -> {
                updateTimeWaveView(
                    isIncrease = false,
                    isStart = false,
                    view = fragmentCutBinding.fragmentCutterReductionEndTimeIv
                )
            }
        }
        return true
    }

    //isIncrease -> true increase, false reduction
    //isStart -> true timeStart, false timeEnd
    private fun updateTimeWaveView(isIncrease: Boolean, isStart: Boolean, view: View) {
        var mEditView = fragmentCutBinding.fragmentCutterWaveEditView
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
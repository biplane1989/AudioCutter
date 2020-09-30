package com.example.audiocutter.functions.fragmentCutterScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FragmentAudioCutBinding
import com.example.audiocutter.ui.fragment_cut.WaveformEditView
import com.example.audiocutter.util.Utils

class AudioCutFragment : BaseFragment(), WaveformEditView.WaveformEditListener,
    View.OnClickListener {
    private lateinit var pathAudio: String

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
        fragmentCutBinding.fragmentCutterWaveEditView.apply {
            setDataSource(pathAudio)
            setListener(this@AudioCutFragment)
        }
    }

    private fun setClick() {
        fragmentCutBinding.fragmentCutterOptionIv.setOnClickListener(this)
        fragmentCutBinding.fragmentCutterTickIv.setOnClickListener(this)
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
    }

    override fun onEndTimeChanged(endTimeMs: Long) {
    }

    override fun onPlayPositionChanged(positionMs: Int) {
    }

    override fun onClick(v: View?) {
        when (v) {
            fragmentCutBinding.fragmentCutterCloseIv -> {
            }
            fragmentCutBinding.fragmentCutterOptionIv -> {
                fragmentCutBinding.fragmentCutterWaveEditView.zoomInt()
            }
            fragmentCutBinding.fragmentCutterTickIv -> {
                fragmentCutBinding.fragmentCutterWaveEditView.zoomOut()
            }
        }
    }

}
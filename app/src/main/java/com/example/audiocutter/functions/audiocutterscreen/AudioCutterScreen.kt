package com.example.audiocutter.functions.audiocutterscreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment

class AudioCutterScreen : BaseFragment() {
    lateinit var mView: View
    lateinit var rvAudioCutter: RecyclerView
    lateinit var audioCutterAdapter: AudiocutterAdapter
    lateinit var audioCutterModel: AudioCutterModel

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioCutterAdapter = AudiocutterAdapter(requireContext())
        audioCutterModel = ViewModelProvider(this).get(AudioCutterModel::class.java)
        observerViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.audio_cutter_screen, container, false)

        initViews()
        return mView
    }

    private fun initViews() {
        rvAudioCutter = mView.findViewById(R.id.rv_audiocutter)
        initLists()

    }

    private fun observerViewModel() {
        runOnUI {
            audioCutterModel.getListAllFileByType(requireContext()).observe(this, Observer {
                audioCutterAdapter.submitList(it)
            })
        }

    }

    private fun initLists() {
        rvAudioCutter.adapter = audioCutterAdapter
        rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())
    }

}
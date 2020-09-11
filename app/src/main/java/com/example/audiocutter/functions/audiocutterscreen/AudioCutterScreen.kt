package com.example.audiocutter.functions.audiocutterscreen

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.objects.AudioFile

class AudioCutterScreen : BaseFragment(), AudiocutterAdapter.AudioCutterListtener {
    val TAG = AudioCutterScreen::class.java.name
    private lateinit var mView: View
    private lateinit var rvAudioCutter: RecyclerView
    private lateinit var audioCutterAdapter: AudiocutterAdapter
    private lateinit var audioCutterModel: AudioCutterModel


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        audioCutterModel = ViewModelProvider(this).get(AudioCutterModel::class.java)
        observerViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.audio_cutter_screen, container, false)
        AudioFileManagerImpl.registerContentObserVerDeleted()
        initViews()
        return mView
    }

    private fun initViews() {

        audioCutterAdapter = AudiocutterAdapter(activity as Activity)
        audioCutterAdapter.setAudioCutterListtener(this)
        rvAudioCutter = mView.findViewById(R.id.rv_audiocutter)
        initLists()

    }

    private fun observerViewModel() {
        runOnUI {
            audioCutterModel.getAllFileByType().observe(this, Observer {
                audioCutterAdapter.submitList(it)
                Log.d(TAG, "observerViewModel: ${it.size}")
                audioCutterAdapter.notifyDataSetChanged()
            })
        }

    }

    private fun initLists() {
        rvAudioCutter.adapter = audioCutterAdapter
        rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun controllerMusic(audioFile: AudioFile, ivController: ImageView) {

    }



}
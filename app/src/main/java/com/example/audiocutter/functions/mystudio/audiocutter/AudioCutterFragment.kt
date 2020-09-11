package com.example.audiocutter.functions.mystudio.audiocutter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.util.ContentLengthInputStream
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.fragment_audio_cutter.*

class AudioCutterFragment : BaseFragment(), AudioCutterScreenCallback {

    val TAG = "001"
    lateinit var audioCutterViewModel: AudioCutterViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    private val playerInfoObserver = Observer<PlayerInfo> {
        Log.d("taih", "playerState ${it.playerState} position ${it.position}")
        audioCutterAdapter.updateMedia(it)
    }

    companion object {
        fun newInstance(): AudioCutterFragment = AudioCutterFragment()
    }

    fun init() {
        rv_list_audio_cutter.layoutManager = LinearLayoutManager(context)
        rv_list_audio_cutter.setHasFixedSize(true)
        rv_list_audio_cutter.adapter = audioCutterAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        audioCutterViewModel = ViewModelProviders.of(this).get(AudioCutterViewModel()::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)

        audioCutterViewModel.getListMusic()?.observe(this, Observer { listMusic ->
            listMusic?.let {
                audioCutterAdapter.submitList(ArrayList(listMusic))
            }
        })
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_audio_cutter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        runOnUI {
            // show loading
            audioCutterViewModel.getData()
            // hide loadding
        }
    }

    override fun play(audioFile: AudioFile) {
        runOnUI {
            ManagerFactory.getAudioPlayer().play(audioFile)
        }
    }

    override fun pause() {
        ManagerFactory.getAudioPlayer().pause()
    }

    override fun resume() {
        ManagerFactory.getAudioPlayer().resume()
    }
}
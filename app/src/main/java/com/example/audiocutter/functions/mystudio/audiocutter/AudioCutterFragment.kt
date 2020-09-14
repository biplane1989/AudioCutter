package com.example.audiocutter.functions.mystudio.audiocutter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.util.ContentLengthInputStream
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.fragment_audio_cutter.*
import kotlinx.android.synthetic.main.output_audio_manager_screen.*

class AudioCutterFragment() : BaseFragment(),
    AudioCutterScreenCallback {

    val TAG = "001"
    lateinit var audioCutterViewModel: AudioCutterViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    var visibilityDeleteStatus = false

    var deleteItemStatus: MutableLiveData<DeleteState> = MutableLiveData()

    private val deleteItemObserver = Observer<DeleteState> {
        audioCutterAdapter.updateDeleteStatus(it)
        when (it) {
            DeleteState.HIDE -> {
                cl_delete_all.visibility = View.GONE
                visibilityDeleteStatus = false
            }
            DeleteState.UNCHECK -> {
                cl_delete_all.visibility = View.VISIBLE
                iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
            }
            DeleteState.CHECKED -> {
                iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
                cl_delete_all.visibility = View.VISIBLE
            }

        }
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioCutterAdapter.updateMedia(it)
    }

    companion object {
        fun newInstance(): AudioCutterFragment =
            AudioCutterFragment()
    }

    fun init() {
        rv_list_audio_cutter.layoutManager = LinearLayoutManager(context)
        rv_list_audio_cutter.setHasFixedSize(true)
        rv_list_audio_cutter.adapter = audioCutterAdapter

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        audioCutterViewModel =
            ViewModelProviders.of(this).get(AudioCutterViewModel()::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)

        audioCutterViewModel.getListMusic()?.observe(this, Observer { listMusic ->
            listMusic?.let {
                audioCutterAdapter.submitList(ArrayList(listMusic))
            }
        })
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
        deleteItemStatus.observe(this, deleteItemObserver)

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
            pb_audio_cutter.visibility = View.VISIBLE
            audioCutterViewModel.getData()
            pb_audio_cutter.visibility = View.GONE
            // hide loadding
        }

        iv_check.setOnClickListener(View.OnClickListener {
            visibilityDeleteStatus = !visibilityDeleteStatus
            if (visibilityDeleteStatus) {
                deleteItemStatus.postValue(DeleteState.CHECKED)
            } else {
                deleteItemStatus.postValue(DeleteState.UNCHECK)
            }
        })
    }

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {

        when (fragmentMeta.action) {
            Constance.ACTION_DELETE -> {
                deleteItemStatus.postValue(DeleteState.UNCHECK)
            }
            Constance.ACTION_CANCEL_DELETE -> {
                deleteItemStatus.postValue(DeleteState.HIDE)
            }
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

    override fun stop() {
        ManagerFactory.getAudioPlayer().stop()
    }

    override fun seekTo(position: Int) {
        ManagerFactory.getAudioPlayer().seek(position)
    }
}
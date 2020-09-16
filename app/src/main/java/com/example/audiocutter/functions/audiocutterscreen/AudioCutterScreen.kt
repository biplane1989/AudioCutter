package com.example.audiocutter.functions.audiocutterscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.audiocutter.functions.audiocutterscreen.view.SetAsDialog
import com.example.audiocutter.functions.audiocutterscreen.view.TypeAudioSetAs
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.audio_cutter_screen.*

class AudioCutterScreen : BaseFragment(), AudiocutterAdapter.AudioCutterListener,
    SetAsDialog.setAsListener {
    val KEY_AUDIO = "KEY_AUDIO"
    val TAG = AudioCutterScreen::class.java.name
    private lateinit var mView: View
    private lateinit var rvAudioCutter: RecyclerView
    private lateinit var audioCutterAdapter: AudiocutterAdapter
    private lateinit var audioCutterModel: AudioCutterModel
    lateinit var dialog: SetAsDialog
    lateinit var audioCutterItem: AudioCutterView

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioCutterAdapter.mediaInfoUpdate(it)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
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

        dialog = SetAsDialog(requireContext())
        audioCutterAdapter = AudiocutterAdapter(activity as Activity)
        audioCutterAdapter.setAudioCutterListtener(this)
        rvAudioCutter = mView.findViewById(R.id.rv_audiocutter)
        initLists()

    }

    private fun observerViewModel() {
        runOnUI {
            audioCutterModel.getAllAudioFile().observe(this, Observer {
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

    override fun play(audioFile: AudioFile) {
        runOnUI {
            Log.d("sesm", "play: ")
            ManagerFactory.getAudioPlayer().play(audioFile)
        }

    }

    override fun pause() {
        Log.d("sesm", "pause: ")
        ManagerFactory.getAudioPlayer().pause()
    }

    override fun resume() {
        Log.d("sesm", "resume: ")
        ManagerFactory.getAudioPlayer().resume()
    }


    override fun stop() {
        Log.d("sesm", "stop: ")
        ManagerFactory.getAudioPlayer().stop()
    }

    override fun showDialogSetAs(itemAudio: AudioCutterView) {
        audioCutterItem = itemAudio
        dialog.setOnCallBack(this)
        dialog.show()
    }

    override fun setAudioAs(typeAudioSetAs: TypeAudioSetAs) {
        Log.d(TAG, "setAudioAs: ${audioCutterItem.audioFile.fileName}")
        when (typeAudioSetAs) {

            TypeAudioSetAs.RINGTONE -> RingtonManagerImpl.setRingTone(
                requireContext(),
                audioFile = audioCutterItem.audioFile
            )
            TypeAudioSetAs.ALARM -> RingtonManagerImpl.setAlarmManager(
                requireContext(),
                audioFile = audioCutterItem.audioFile
            )
            TypeAudioSetAs.NOTIFICATION -> RingtonManagerImpl.setNotificationSound(
                requireContext(),
                audioFile = audioCutterItem.audioFile
            )
        }
        dialog.dismiss()
    }





}



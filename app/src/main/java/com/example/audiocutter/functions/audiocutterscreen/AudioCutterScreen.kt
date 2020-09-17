package com.example.audiocutter.functions.audiocutterscreen

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
import com.example.audiocutter.functions.audiocutterscreen.view.SetAsDoneDialog
import com.example.audiocutter.functions.audiocutterscreen.view.TypeAudioSetAs
import com.example.audiocutter.objects.AudioFile

class AudioCutterScreen : BaseFragment(), AudiocutterAdapter.AudioCutterListener,
    SetAsDialog.setAsListener, View.OnClickListener {
    val TAG = AudioCutterScreen::class.java.name
    private lateinit var mView: View
    private lateinit var rvAudioCutter: RecyclerView
    private lateinit var audioCutterAdapter: AudiocutterAdapter
    private lateinit var audioCutterModel: AudioCutterModel
    lateinit var dialog: SetAsDialog
    lateinit var dialogDone: SetAsDoneDialog
    lateinit var audioCutterItem: AudioCutterView
    lateinit var ivFile: ImageView
    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    var isCheckList = true

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
        ivFile = mView.findViewById(R.id.iv_file)
        ivFile.setOnClickListener(this)
        dialog = SetAsDialog(requireContext())
        dialogDone = SetAsDoneDialog(requireContext())
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
//                audioCutterAdapter.notifyDataSetChanged()
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
        var rs = false
        Log.d(TAG, "setAudioAs: ${audioCutterItem.audioFile.fileName}")
        when (typeAudioSetAs) {

            TypeAudioSetAs.RINGTONE -> {
                rs = RingtonManagerImpl.setRingTone(
                    requireContext(),
                    audioFile = audioCutterItem.audioFile
                )
            }
            TypeAudioSetAs.ALARM -> {
                rs = RingtonManagerImpl.setAlarmManager(
                    requireContext(),
                    audioFile = audioCutterItem.audioFile
                )
            }
            TypeAudioSetAs.NOTIFICATION -> {
                rs = RingtonManagerImpl.setNotificationSound(
                    requireContext(),
                    audioFile = audioCutterItem.audioFile
                )
            }
        }
        if (rs) {
            dialog.dismiss()
            dialogDone.show()
        } else {
            Toast.makeText(requireContext(), "set as fail", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_file -> updateAllFile()
        }
    }

    private fun updateAllFile() {
        stop()
        runOnUI {
            try {
                if (isCheckList) {
                    audioCutterModel.getAllFileByType().observe(this, Observer {
                        listTmp.clear()
                        listTmp = it.toMutableList()
                        audioCutterAdapter.submitList(listTmp)
                        isCheckList = false

                    })
                } else {
                    audioCutterModel.getAllAudioFile().observe(this, Observer {
                        listTmp.clear()
                        listTmp = it.toMutableList()
                        audioCutterAdapter.submitList(listTmp)
                        isCheckList = true
                    })
                }

                Log.d(TAG, "updateAllFile: check $isCheckList    listSize ${listTmp.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}



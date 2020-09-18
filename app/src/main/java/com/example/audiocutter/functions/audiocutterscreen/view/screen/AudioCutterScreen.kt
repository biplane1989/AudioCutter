package com.example.audiocutter.functions.audiocutterscreen.view.screen

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
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.core.rington.RingtonManagerImpl
import com.example.audiocutter.functions.audiocutterscreen.dialog.SetAsDialog
import com.example.audiocutter.functions.audiocutterscreen.dialog.SetAsDoneDialog
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.audiocutterscreen.objs.TypeAudioSetAs
import com.example.audiocutter.functions.audiocutterscreen.view.adapter.AudiocutterAdapter

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

    val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        audioCutterAdapter.submitList(ArrayList(listMusic))
    }

    private val playerInfoObserver = Observer<PlayerInfo> {

        if (audioCutterModel.isPlayingStatus) {
            audioCutterAdapter.submitList(audioCutterModel.updateMediaInfo(it))
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioCutterAdapter = AudiocutterAdapter(requireContext())

        audioCutterModel = ViewModelProvider(this).get(AudioCutterModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val listAudioViewLiveData = audioCutterModel.getAllAudioFile()
            listAudioViewLiveData.removeObserver(listAudioObserver)
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
        }
    }

    private fun initViews() {
        ivFile = mView.findViewById(R.id.iv_file)
        ivFile.setOnClickListener(this)
        dialog = SetAsDialog(requireContext())
        dialogDone = SetAsDoneDialog(requireContext())

        audioCutterAdapter.setAudioCutterListtener(this)
        rvAudioCutter = mView.findViewById(R.id.rv_audiocutter)


    }


    private fun initLists() {
        rvAudioCutter.adapter = audioCutterAdapter
        rvAudioCutter.setHasFixedSize(true)
        rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun play(pos: Int) {
        val state = PlayerState.IDLE
        audioCutterAdapter.submitList(audioCutterModel.controllerAudio(pos, state))

    }

    override fun pause(pos: Int) {
        val state = PlayerState.PLAYING
        audioCutterAdapter.submitList(audioCutterModel.controllerAudio(pos, state))
    }

    override fun resume(pos: Int) {
        val state = PlayerState.PAUSE
        audioCutterAdapter.submitList(audioCutterModel.controllerAudio(pos, state))
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
//                    audioFile = AudioFile(File(""),"dd",0,0,0, Uri.parse("l"))
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
//            R.id.iv_file -> updateAllFile()
        }
    }


    private fun updateAllFile() {

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





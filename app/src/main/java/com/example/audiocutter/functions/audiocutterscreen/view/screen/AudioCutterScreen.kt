package com.example.audiocutter.functions.audiocutterscreen.view.screen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
    lateinit var ivSearch: ImageView
    lateinit var ivBack: ImageView
    lateinit var tvAudioScreen: TextView
    lateinit var tvEmptyList: TextView
    lateinit var ivClose: ImageView
    lateinit var edtSearch: EditText


    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    var mlistSearch: MutableList<AudioCutterView> = mutableListOf()
    var isCheckList = true

    val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
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
        mlistSearch.clear()
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
        checkEdtSearchAudio()
        return mView
    }

    private fun checkEdtSearchAudio() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                seachAudioByName(edtSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun seachAudioByName(yourTextSearch: String) {
        rvAudioCutter.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioCutterAdapter.submitList(listTmp)
        }
        if (audioCutterModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioCutterAdapter.submitList(audioCutterModel.getListsearch())
            Log.d(TAG, "seachAudioByName: ${audioCutterModel.getListsearch().size}")
        } else {
            mlistSearch.clear()
            rvAudioCutter.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
        }
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

        ivFile = mView.findViewById(R.id.iv_audiocutter_screen_file)
        ivBack = mView.findViewById(R.id.iv_audiocutter_screen_back)
        ivClose = mView.findViewById(R.id.iv_audiocutter_screen_close)
        ivSearch = mView.findViewById(R.id.iv_audiocutter_screen_search)
        tvAudioScreen = mView.findViewById(R.id.tv_audiocutter_screen)
        tvEmptyList = mView.findViewById(R.id.tv_empty_list)
        edtSearch = mView.findViewById(R.id.edt_auciocutter_search)

        ivFile.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        dialog = SetAsDialog(requireContext())
        dialogDone = SetAsDoneDialog(requireContext())

        audioCutterAdapter.setAudioCutterListtener(this)
        rvAudioCutter = mView.findViewById(R.id.rv_audiocutter)


    }

    fun hideOrShowEditText(status: Int) {
        ivClose.visibility = status
        edtSearch.visibility = status
    }

    fun hideOrShowView(status: Int) {
        ivSearch.visibility = status
        tvAudioScreen.visibility = status
        ivFile.visibility = status
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
//                    requireContext(),
                    audioFile = audioCutterItem.audioFile
//                    audioFile = AudioFile(File(""),"dd",0,0,0, Uri.parse("l"))
                )
            }
            TypeAudioSetAs.ALARM -> {
                rs = RingtonManagerImpl.setAlarmManager(
//                    requireContext(),
                    audioFile = audioCutterItem.audioFile
                )
            }
            TypeAudioSetAs.NOTIFICATION -> {
                rs = RingtonManagerImpl.setNotificationSound(
//                    requireContext(),
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
            R.id.iv_audiocutter_screen_file -> updateAllFile()
            R.id.iv_audiocutter_screen_search -> searchAudiofile()
            R.id.iv_audiocutter_screen_close -> previousStatus()
        }
    }

    private fun previousStatus() {
        rvAudioCutter.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        audioCutterAdapter.submitList(listTmp)
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }


    private fun updateAllFile() {
        runOnUI {
            try {
                if (isCheckList) {
                    audioCutterModel.getAllFileByType().observe(this, Observer {
                        listTmp.clear()
                        listTmp.addAll(it.toMutableList())
                        audioCutterAdapter.submitList(listTmp)
                        isCheckList = false
                    })
                } else {
                    audioCutterModel.getAllAudioFile().observe(this, Observer {
                        listTmp.clear()
                        listTmp.addAll(it.toMutableList())
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





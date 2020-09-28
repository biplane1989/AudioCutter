package com.example.audiocutter.functions.audiocutterscreen.view.screen

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.example.audiocutter.core.manager.PlayerInfo
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
    lateinit var ivBackEdt: ImageView
    lateinit var tvAudioScreen: TextView
    lateinit var tvEmptyList: TextView
    lateinit var ivClose: ImageView
    lateinit var edtSearch: EditText
    var currentPos = -1


    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    var isCheckList = true

    val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioCutterAdapter.submitList(ArrayList(listMusic))
    }

    private val playerInfoObserver = Observer<PlayerInfo> {

            audioCutterAdapter.submitList(audioCutterModel.updateMediaInfo(it))
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
        ManagerFactory.getAudioFileManagerImpl().registerContentObserVerDeleted()
        initViews()
        checkEdtSearchAudio()
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

    private fun checkEdtSearchAudio() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchAudioByName(edtSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        rvAudioCutter.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioCutterAdapter.submitList(listTmp)
        }
        if (audioCutterModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioCutterAdapter.submitList(audioCutterModel.getListsearch())
            Log.d(TAG, "seachAudioByName: ${audioCutterModel.getListsearch().size}")
        } else {
            rvAudioCutter.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
        }
    }


    private fun initViews() {

        ivFile = mView.findViewById(R.id.iv_audio_cutter_screen_file)
        ivBack = mView.findViewById(R.id.iv_cutter_screen_back)
        ivBackEdt = mView.findViewById(R.id.iv_cutter_screen_back_edt)
        ivClose = mView.findViewById(R.id.iv_cutter_screen_close)
        ivSearch = mView.findViewById(R.id.iv_cutter_screen_search)
        tvAudioScreen = mView.findViewById(R.id.tv_cutter_screen)
        tvEmptyList = mView.findViewById(R.id.tv_empty_list_cutter)
        edtSearch = mView.findViewById(R.id.edt_cutter_search)

        ivFile.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivBackEdt.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        dialog = SetAsDialog(requireContext())
        dialogDone = SetAsDoneDialog(requireContext())

        audioCutterAdapter.setAudioCutterListtener(this)
        rvAudioCutter = mView.findViewById(R.id.rv_audio_cutter)

    }

    fun hideOrShowEditText(status: Int) {
        ivBackEdt.visibility = status
        ivClose.visibility = status
        edtSearch.visibility = status
    }

    fun hideOrShowView(status: Int) {
        ivSearch.visibility = status
        tvAudioScreen.visibility = status
        ivFile.visibility = status
    }


    fun hideKeyBroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun initLists() {
        rvAudioCutter.adapter = audioCutterAdapter
        rvAudioCutter.setHasFixedSize(true)
        rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())
    }


    override fun play(pos: Int) {
        runOnUI {
            currentPos = pos
            audioCutterModel.play(pos)
        }
    }

    override fun pause(pos: Int) {
        currentPos = pos
        audioCutterModel.pause()

    }

    override fun resume(pos: Int) {
        currentPos = pos
        audioCutterModel.resume()
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
            R.id.iv_audio_cutter_screen_file -> updateAllFile()
            R.id.iv_cutter_screen_search -> searchAudiofile()
            R.id.iv_cutter_screen_back_edt -> previousStatus()
            R.id.iv_cutter_screen_close -> clearText()
        }
    }

    private fun clearText() {
        if (!edtSearch.text.toString().isEmpty()) {
            edtSearch.setText("")
        }
    }

    private fun previousStatus() {
        rvAudioCutter.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        audioCutterAdapter.submitList(listTmp)
        hideKeyBroad()
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        showKeybroad()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }


    private fun updateAllFile() {
        runOnUI {
            try {
                if (isCheckList) {
                    ManagerFactory.getAudioPlayer().stop()
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
                if (currentPos != -1) {
                    ManagerFactory.getAudioPlayer().stop()
                }
                Log.d(TAG, "updateAllFile: check $isCheckList    listSize ${listTmp.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getAudioPlayer().stop()
    }


}





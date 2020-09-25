package com.example.audiocutter.functions.ChooseScreen.view

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import com.example.audiocutter.functions.ChooseScreen.adapter.RecentAdapter
import com.example.audiocutter.functions.audiocutterscreen.dialog.SetAsDialog
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen

class RecentAddedScreen : BaseFragment(), View.OnClickListener, RecentAdapter.AudioRecentListener {

    val TAG = AudioCutterScreen::class.java.name
    private lateinit var mView: View
    private lateinit var rvAudioRecent: RecyclerView
    private lateinit var audioRecentAdapter: RecentAdapter
    private lateinit var audioRecentModel: RecentModel
    lateinit var dialog: SetAsDialog
    lateinit var ivFile: ImageView
    lateinit var ivSearch: ImageView
    lateinit var ivBack: ImageView
    lateinit var ivBackEdt: ImageView
    lateinit var tbName: TableRow
    lateinit var tvEmptyList: TextView
    lateinit var ivClose: ImageView
    lateinit var edtSearch: EditText
    var currentPos = -1

    //rlt_next_recent_parent

    lateinit var ivNextRecent: ImageView
    lateinit var tvNextRecent: TextView
    lateinit var rltNextRecent: RelativeLayout
    lateinit var rltNextRecentParent: RelativeLayout

    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    var isCheckList = true

    val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioRecentAdapter.submitList(ArrayList(listMusic))
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        if (audioRecentModel.isPlayingStatus) {
            audioRecentAdapter.submitList(audioRecentModel.updateMediaInfo(it))
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioRecentAdapter = RecentAdapter(requireContext())
        audioRecentModel = ViewModelProvider(this).get(RecentModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.recent_added_screen, container, false)
        AudioFileManagerImpl.registerContentObserVerDeleted()
        initViews()
        checkEdtSearchAudio()
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val listAudioViewLiveData = audioRecentModel.getAllAudioFile()
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
        rvAudioRecent.visibility = View.VISIBLE
        rltNextRecentParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioRecentAdapter.submitList(listTmp)
        }
        if (audioRecentModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioRecentAdapter.submitList(audioRecentModel.getListsearch())
        } else {
            rvAudioRecent.visibility = View.GONE
            rltNextRecentParent.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
        }
    }


    private fun initViews() {

        rltNextRecent = mView.findViewById(R.id.rlt_next_recent)
        rltNextRecentParent = mView.findViewById(R.id.rlt_next_recent_parent)
        ivNextRecent = mView.findViewById(R.id.iv_next_recent)
        tvNextRecent = mView.findViewById(R.id.tv_next_recent)

        ivFile = mView.findViewById(R.id.iv_audiorecent_screen_file)
        ivBack = mView.findViewById(R.id.iv_recent_screen_back)
        ivBackEdt = mView.findViewById(R.id.iv_recent_screen_back_edt)
        ivClose = mView.findViewById(R.id.iv_recent_screen_close)
        ivSearch = mView.findViewById(R.id.iv_recent_screen_search)
        tbName = mView.findViewById(R.id.tb_name_recent)
        tvEmptyList = mView.findViewById(R.id.tv_empty_list_recent)
        edtSearch = mView.findViewById(R.id.edt_recent_search)

        ivFile.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivBackEdt.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        rltNextRecent.setOnClickListener(this)

        audioRecentAdapter.setAudioCutterListtener(this)
        rvAudioRecent = mView.findViewById(R.id.rv_recent)

    }

    fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    fun hideOrShowEditText(status: Int) {
        ivBackEdt.visibility = status
        ivClose.visibility = status
        edtSearch.visibility = status
    }

    fun hideOrShowView(status: Int) {
        ivSearch.visibility = status
        tbName.visibility = status
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


    private fun initLists() {
        rvAudioRecent.adapter = audioRecentAdapter
        rvAudioRecent.setHasFixedSize(true)
        rvAudioRecent.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun play(pos: Int) {
        currentPos = pos
        val state = PlayerState.IDLE
        audioRecentAdapter.submitList(audioRecentModel.controllerAudio(pos, state))

    }

    override fun pause(pos: Int) {
        currentPos = pos
        val state = PlayerState.PLAYING
        audioRecentAdapter.submitList(audioRecentModel.controllerAudio(pos, state))

    }

    override fun resume(pos: Int) {
        currentPos = pos
        val state = PlayerState.PAUSE
        audioRecentAdapter.submitList(audioRecentModel.controllerAudio(pos, state))
    }

    override fun chooseItemAudio(pos: Int, rs: Boolean) {
        audioRecentAdapter.submitList(audioRecentModel.changeItemAudioFile(pos, rs))

        if (audioRecentModel.checkList()) {
            setColorForView(R.color.colorWhite, R.drawable.bg_next_recent_audio_enabled, true)
        } else {
            setColorForView(R.color.colorBlack, R.drawable.bg_next_recent_audio_disabled, false)
        }
        if (audioRecentModel.isCheckItem) {
            showToast("You can select only 2 item")
        }
    }


    private fun setColorForView(color: Int, bg: Int, rs: Boolean) {
        rltNextRecent.isEnabled = rs
        rltNextRecent.setBackgroundResource(bg)
        ivNextRecent.setColorFilter(requireActivity().resources.getColor(color));
        tvNextRecent.setTextColor(requireActivity().resources.getColor(color))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_audiorecent_screen_file -> changeListFile()
            R.id.iv_recent_screen_search -> searchAudiofile()
            R.id.iv_recent_screen_back_edt -> previousStatus()
            R.id.iv_recent_screen_close -> clearText()
            R.id.rlt_next_recent -> handleAudiofile()
        }
    }

    private fun handleAudiofile() {
        val listItemHandle = audioRecentModel.getListItemChoose()

//        listItemHandle.forEach {
//            Log.d(TAG, "handleAudiofile: ${it.audioFile.fileName}")
//        }

        /**place handle listItem choose*/

    }

    private fun clearText() {
        if (!edtSearch.text.toString().isEmpty()) {
            edtSearch.setText("")
        }
    }

    private fun previousStatus() {
        rvAudioRecent.visibility = View.VISIBLE
        rltNextRecentParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        audioRecentAdapter.submitList(listTmp)
        hideKeyBroad()
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        showKeybroad()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }


    private fun changeListFile() {


        setColorForView(R.color.colorBlack, R.drawable.bg_next_recent_audio_disabled, false)
        runOnUI {
            try {
                if (isCheckList) {
                    ManagerFactory.getAudioPlayer().stop()
                    audioRecentModel.getAllFileByType().observe(this, Observer {
                        listTmp.clear()
                        listTmp.addAll(it.toMutableList())
                        audioRecentAdapter.submitList(listTmp)
                        isCheckList = false
                    })
                } else {
                    audioRecentModel.getAllAudioFile().observe(this, Observer {
                        listTmp.clear()
                        listTmp.addAll(it.toMutableList())
                        audioRecentAdapter.submitList(listTmp)
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





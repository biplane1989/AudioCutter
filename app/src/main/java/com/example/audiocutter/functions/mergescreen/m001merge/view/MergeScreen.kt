package com.example.audiocutter.functions.mergescreen.m001merge.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.mergescreen.event.OnActionCallback
import com.example.audiocutter.functions.mergescreen.m001merge.adapter.MergeAdapter

class MergeScreen : BaseFragment(), View.OnClickListener, MergeAdapter.AudioMergeListener {
    private lateinit var mView: View
    private lateinit var rvAudioMer: RecyclerView
    private lateinit var audioMerAdapter: MergeAdapter
    private lateinit var audioMerModel: MergeModel
    lateinit var ivFile: ImageView
    lateinit var ivSearch: ImageView
    lateinit var ivBack: ImageView
    lateinit var ivBackEdt: ImageView
    lateinit var tbName: TableRow
    lateinit var tvEmptyList: TextView
    lateinit var ivClose: ImageView
    lateinit var ivEmptyList: ImageView
    lateinit var edtSearch: EditText
    var currentPos = -1

//rlt_next_recent_parent

    lateinit var ivNextMer: ImageView
    lateinit var tvNextMer: TextView
    lateinit var tvCountFile: TextView
    lateinit var rltNextMer: RelativeLayout
    lateinit var rltNextMerParent: RelativeLayout

    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    lateinit var mCallback: OnActionCallback

    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioMerAdapter.submitList(ArrayList(listMusic))

    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioMerAdapter.submitList(audioMerModel.updateMediaInfo(it))
    }

    fun setOnCalBack(event: OnActionCallback) {
        mCallback = event
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerAdapter = MergeAdapter(requireContext())
        audioMerModel = ViewModelProvider(this).get(MergeModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.merge_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val listAudioViewLiveData = audioMerModel.getAllAudioFile()
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
        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        tvCountFile.text = getString(R.string.countFile)
        rvAudioMer.visibility = View.VISIBLE
        rltNextMerParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        ivEmptyList.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioMerAdapter.submitList(listTmp)
        }
        if (audioMerModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioMerAdapter.submitList(audioMerModel.getListsearch())
        } else {
            rvAudioMer.visibility = View.GONE
            rltNextMerParent.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
            ivEmptyList.visibility = View.VISIBLE
        }
    }


    private fun initViews() {

        rltNextMer = mView.findViewById(R.id.rlt_next_mer)
        rltNextMer.isEnabled = false
        rltNextMerParent = mView.findViewById(R.id.rlt_next_mer_parent)
        ivNextMer = mView.findViewById(R.id.iv_next_mer)
        ivEmptyList = mView.findViewById(R.id.iv_empty_list_merge)
        tvNextMer = mView.findViewById(R.id.tv_next_mer)
        tvCountFile = mView.findViewById(R.id.tv_count_file_mer)

        ivFile = mView.findViewById(R.id.iv_audio_mer_screen_file)
        ivBack = mView.findViewById(R.id.iv_mer_screen_back)
        ivBackEdt = mView.findViewById(R.id.iv_mer_screen_back_edt)
        ivClose = mView.findViewById(R.id.iv_mer_screen_close)
        ivSearch = mView.findViewById(R.id.iv_mer_screen_search)
        tbName = mView.findViewById(R.id.tb_name_mer)
        tvEmptyList = mView.findViewById(R.id.tv_empty_list_mer)
        edtSearch = mView.findViewById(R.id.edt_mer_search)

        ivFile.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivBackEdt.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        rltNextMer.setOnClickListener(this)
        audioMerAdapter.setAudioListener(this)
        rvAudioMer = mView.findViewById(R.id.rv_merge)


    }

    private fun showKeybroad() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun hideOrShowEditText(status: Int) {
        ivBackEdt.visibility = status
        ivClose.visibility = status
        edtSearch.visibility = status
        val a = LinearLayoutManager.HORIZONTAL
    }

    private fun hideOrShowView(status: Int) {
        ivSearch.visibility = status
        tbName.visibility = status
        ivFile.visibility = status
    }


    private fun hideKeyBroad() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initLists() {


        rvAudioMer.adapter = audioMerAdapter
        rvAudioMer.setHasFixedSize(true)
        rvAudioMer.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun play(pos: Int) {
        runOnUI {
            currentPos = pos
            audioMerModel.play(pos)
        }
    }

    override fun pause(pos: Int) {
        currentPos = pos
        audioMerModel.pause()

    }

    override fun resume(pos: Int) {
        currentPos = pos
        audioMerModel.resume()
    }

    @SuppressLint("SetTextI18n")
    override fun chooseItemAudio(pos: Int, rs: Boolean) {
        audioMerAdapter.submitList(audioMerModel.chooseItemAudioFile(pos, rs))

        if (audioMerModel.checkList() >= 2) {
            setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
        } else {
            setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        }

        tvCountFile.text = "${audioMerModel.checkList()} file"
    }


    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        rltNextMer.isEnabled = rs
        rltNextMer.setBackgroundResource(bg)
        ivNextMer.setColorFilter(requireActivity().resources.getColor(color));
        tvNextMer.setTextColor(requireActivity().resources.getColor(color))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_mer_screen_search -> searchAudiofile()
            R.id.iv_mer_screen_back_edt -> previousStatus()
            R.id.iv_mer_screen_close -> {
                if (!edtSearch.text.toString().isEmpty()) {
                    edtSearch.setText("")
                }
            }
            R.id.rlt_next_mer -> handleAudiofile()
        }
    }

    private fun handleAudiofile() {
        ManagerFactory.getAudioPlayer().stop()
        val listItemHandle = audioMerModel.getListItemChoose()
        mCallback.sendAndReceiveData(listItemHandle)
    }


    private fun previousStatus() {
        edtSearch.setText("")
        rvAudioMer.visibility = View.VISIBLE
        rltNextMerParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        ivEmptyList.visibility = View.GONE
        audioMerAdapter.submitList(listTmp)
        hideKeyBroad()
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        showKeybroad()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getAudioPlayer().stop()
    }


}





package com.example.audiocutter.functions.mixscreen.view

import android.annotation.SuppressLint
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
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView
import com.example.audiocutter.functions.audiocutterscreen.view.screen.AudioCutterScreen
import com.example.audiocutter.functions.mixscreen.adapter.MixAdapter

class MixerAudioScreen : BaseFragment(), View.OnClickListener, MixAdapter.AudioMixerListener {

    val TAG = AudioCutterScreen::class.java.name
    private lateinit var mView: View
    private lateinit var rvAudioMix: RecyclerView
    private lateinit var audioMixAdapter: MixAdapter
    private lateinit var audioMixModel: MixModel
    lateinit var ivFile: ImageView
    lateinit var ivEmptyList: ImageView
    lateinit var ivSearch: ImageView
    lateinit var ivBack: ImageView
    lateinit var ivBackEdt: ImageView
    lateinit var tbName: TableRow
    lateinit var tvEmptyList: TextView
    lateinit var ivClose: ImageView
    lateinit var edtSearch: EditText
    var currentPos = -1

    //rlt_next_recent_parent

    lateinit var ivNextMix: ImageView
    lateinit var tvNextMix: TextView
    lateinit var tvCountFile: TextView
    lateinit var rltNextMix: RelativeLayout
    lateinit var rltNextMixParent: RelativeLayout

    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    var isChangeList = true

    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioMixAdapter.submitList(ArrayList(listMusic))

    }

    private val playerInfoObserver = Observer<PlayerInfo> {
            audioMixAdapter.submitList(audioMixModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMixAdapter = MixAdapter(requireContext())
        audioMixModel = ViewModelProvider(this).get(MixModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.mixer_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val listAudioViewLiveData = audioMixModel.getAllAudioFile()
            listAudioViewLiveData.removeObserver(listAudioObserver)
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
        }
    }

    private fun checkEdtSearchAudio() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: $p0 - $p1-  $p2- $p3")
                searchAudioByName(edtSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        tvCountFile.text = getString(R.string.countFile)
        rvAudioMix.visibility = View.VISIBLE
        rltNextMixParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        ivEmptyList.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioMixAdapter.submitList(listTmp)
        }
        if (audioMixModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioMixAdapter.submitList(audioMixModel.getListsearch())
        } else {
            rvAudioMix.visibility = View.GONE
            rltNextMixParent.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
            ivEmptyList.visibility = View.VISIBLE
        }
    }


    private fun initViews() {

        rltNextMix = mView.findViewById(R.id.rlt_next_mixer)
        rltNextMixParent = mView.findViewById(R.id.rlt_next_mixer_parent)
        ivNextMix = mView.findViewById(R.id.iv_next_mixer)
        tvNextMix = mView.findViewById(R.id.tv_next_mixer)
        tvCountFile = mView.findViewById(R.id.tv_count_file)

        ivFile = mView.findViewById(R.id.iv_audio_mixer_screen_file)
        ivEmptyList = mView.findViewById(R.id.iv_empty_list_mixer)
        ivBack = mView.findViewById(R.id.iv_mixer_screen_back)
        ivBackEdt = mView.findViewById(R.id.iv_mixer_screen_back_edt)
        ivClose = mView.findViewById(R.id.iv_mixer_screen_close)
        ivSearch = mView.findViewById(R.id.iv_mixer_screen_search)
        tbName = mView.findViewById(R.id.tb_name_mixer)
        tvEmptyList = mView.findViewById(R.id.tv_empty_list_mixer)
        edtSearch = mView.findViewById(R.id.edt_mixer_search)

        ivFile.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivBackEdt.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        rltNextMix.setOnClickListener(this)

        audioMixAdapter.setAudioCutterListtener(this)
        rvAudioMix = mView.findViewById(R.id.rv_mixer)


    }

    private fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun hideOrShowEditText(status: Int) {
        ivBackEdt.visibility = status
        ivClose.visibility = status
        edtSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        ivSearch.visibility = status
        tbName.visibility = status
        ivFile.visibility = status
    }


    private fun hideKeyBroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initLists() {


        rvAudioMix.adapter = audioMixAdapter
        rvAudioMix.setHasFixedSize(true)
        rvAudioMix.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun play(pos: Int) {
        runOnUI {
            currentPos = pos
            audioMixModel.play(pos)
        }
    }

    override fun pause(pos: Int) {
        currentPos = pos
        audioMixModel.pause()

    }

    override fun resume(pos: Int) {
        currentPos = pos
        audioMixModel.resume()
    }

    @SuppressLint("SetTextI18n")
    override fun chooseItemAudio(pos: Int, rs: Boolean) {
        audioMixAdapter.submitList(audioMixModel.chooseItemAudioFile(pos, rs))

        if (audioMixModel.checkList() == 2) {
            setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
        } else {
            setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        }
        if (audioMixModel.isChooseItem) {
            showToast(getString(R.string.ToastExceed))
        }
        tvCountFile.text = "${audioMixModel.checkList()} file"
    }


    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        rltNextMix.isEnabled = rs
        rltNextMix.setBackgroundResource(bg)
        ivNextMix.setColorFilter(requireActivity().resources.getColor(color));
        tvNextMix.setTextColor(requireActivity().resources.getColor(color))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_mixer_screen_search -> searchAudiofile()
            R.id.iv_mixer_screen_back_edt -> previousStatus()
            R.id.iv_mixer_screen_close -> clearText()
            R.id.rlt_next_mixer -> handleAudiofile()
        }
    }

    private fun handleAudiofile() {
        val listItemHandle = audioMixModel.getListItemChoose()

        listItemHandle.forEach {
            Log.d(TAG, "handleAudiofile: ${it.audioFile.fileName}")
        }


        /**place handle listItem choose*/

    }

    private fun clearText() {
        if (!edtSearch.text.toString().isEmpty()) {
            edtSearch.setText("")
        }
    }

    private fun previousStatus() {
        edtSearch.setText("")
        rvAudioMix.visibility = View.VISIBLE
        rltNextMixParent.visibility = View.VISIBLE
        tvEmptyList.visibility = View.GONE
        ivEmptyList.visibility = View.GONE
        audioMixAdapter.submitList(listTmp)
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





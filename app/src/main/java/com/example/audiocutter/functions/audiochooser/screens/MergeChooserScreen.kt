package com.example.audiocutter.functions.audiochooser.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MergeChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.adapters.MergePreviewAdapter

class MergeChooserScreen : BaseFragment(), View.OnClickListener,
    MergePreviewAdapter.AudioMergeListener {
    private lateinit var mView: View
    private lateinit var binding: MergeChooserScreenBinding

    //    private lateinit var rvAudioMer: RecyclerView
    private lateinit var audioMerAdapter: MergePreviewAdapter
    private lateinit var audioMerModel: MergeChooserModel

    //    lateinit var ivFile: ImageView
//    lateinit var ivSearch: ImageView
//    lateinit var ivBack: ImageView
//    lateinit var ivBackEdt: ImageView
//    lateinit var tbName: TableRow
//    lateinit var tvEmptyList: TextView
//    lateinit var ivClose: ImageView
//    lateinit var ivEmptyList: ImageView
//    lateinit var edtSearch: EditText
    var currentPos = -1

//rlt_next_recent_parent

//    lateinit var ivNextMer: ImageView
//    lateinit var tvNextMer: TextView
//    lateinit var tvCountFile: TextView
//    lateinit var rltNextMer: RelativeLayout
//    lateinit var rltNextMerParent: RelativeLayout

    var listTmp: MutableList<AudioCutterView> = mutableListOf()
    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioMerAdapter.submitList(ArrayList(listMusic))

    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioMerAdapter.submitList(audioMerModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerAdapter =
            MergePreviewAdapter(
                requireContext()
            )
        audioMerModel = ViewModelProvider(this).get(MergeChooserModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.merge_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return binding.root
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
        binding.edtMerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchAudioByName(binding.edtMerSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        binding.tvCountFileMer.text = getString(R.string.countFile)
        binding.rvMerge.visibility = View.VISIBLE
        binding.rltNextMerParent.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.GONE
        binding.ivEmptyListMerge.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioMerAdapter.submitList(listTmp)
        }
        if (audioMerModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioMerAdapter.submitList(audioMerModel.getListsearch())
        } else {
            binding.rvMerge.visibility = View.GONE
            binding.rltNextMerParent.visibility = View.GONE
            binding.tvEmptyListMer.visibility = View.VISIBLE
            binding.ivEmptyListMerge.visibility = View.VISIBLE
        }
    }


    private fun initViews() {

        binding.rltNextMer.isEnabled = false
        binding.ivAudioMerScreenFile.setOnClickListener(this)
        binding.ivMerScreenSearch.setOnClickListener(this)
        binding.ivMerScreenBackEdt.setOnClickListener(this)
        binding.ivMerScreenClose.setOnClickListener(this)
        binding.rltNextMer.setOnClickListener(this)
        binding.ivAudioMerScreenFile.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        audioMerAdapter.setAudioListener(this)


    }

    private fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun hideOrShowEditText(status: Int) {
        binding.ivMerScreenBackEdt.visibility = status
        binding.ivMerScreenClose.visibility = status
        binding.edtMerSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        binding.ivMerScreenSearch.visibility = status
        binding.tvMerScreen.visibility = status
        binding.ivAudioMerScreenFile.visibility = status
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

        binding.rvMerge.adapter = audioMerAdapter
        binding.rvMerge.setHasFixedSize(true)
        binding.rvMerge.layoutManager = LinearLayoutManager(requireContext())

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
            setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
        }

        binding.tvCountFileMer.text = "${audioMerModel.checkList()} file"
    }


    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        binding.rltNextMer.isEnabled = rs
        binding.rltNextMer.setBackgroundResource(bg)
        binding.ivNextMer.setColorFilter(requireActivity().resources.getColor(color));
        binding.tvNextMer.setTextColor(requireActivity().resources.getColor(color))
    }


    override fun onClick(view: View) {
        when (view) {
            binding.ivMerScreenSearch -> {
                searchAudiofile()
            }
            binding.ivMerScreenBackEdt -> {
                previousStatus()
            }
            binding.ivMerScreenClose -> {
                if (!binding.edtMerSearch.text.toString().isEmpty()) {
                    binding.edtMerSearch.setText("")
                }
            }
            binding.rltNextMer -> {
                handleAudiofile()
            }
            binding.ivMerScreenBack -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun handleAudiofile() {
        ManagerFactory.getAudioPlayer().stop()
        val listItemHandle = audioMerModel.getListItemChoose()
//handler
    }


    private fun previousStatus() {
        binding.edtMerSearch.setText("")
        binding.rvMerge.visibility = View.VISIBLE
        binding.rltNextMerParent.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.GONE
        binding.ivEmptyListMerge.visibility = View.GONE
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

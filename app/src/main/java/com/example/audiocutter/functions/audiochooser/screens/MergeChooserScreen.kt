package com.example.audiocutter.functions.audiochooser.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MergeChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.MergeChooserAdapter
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.mystudio.screens.FragmentMeta

class MergeChooserScreen : BaseFragment(), View.OnClickListener, MergeChooserAdapter.AudioMergeListener {
    private lateinit var binding: MergeChooserScreenBinding

    private lateinit var audioMerAdapter: MergeChooserAdapter
    private lateinit var audioMerModel: MergeChooserModel
    var stateObserver = Observer<Int> {
        when (it) {
            1 -> {
                showProgressBar(true)
                binding.ivEmptyListMerge.visibility = View.GONE
                binding.tvEmptyListMer.visibility = View.GONE
            }
            0 -> {
                showProgressBar(false)
            }
            -1 -> {
                showProgressBar(false)
            }
        }
    }
    var currentPos = -1

    private val listAudioObserver = Observer<List<AudioCutterView>> {
        if (it.isEmpty() || it == null) {
            showEmptyList()
        } else {
            audioMerAdapter.submitList(ArrayList(it))
            showList()
        }

    }


    override fun onPause() {
        super.onPause()
        audioMerModel.pause()
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioMerAdapter.submitList(audioMerModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerAdapter = MergeChooserAdapter(requireContext())
        audioMerModel = ViewModelProvider(this).get(MergeChooserModel::class.java)
        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.merge_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            audioMerModel.getAllAudioFile().observe(viewLifecycleOwner, listAudioObserver)
            audioMerModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)

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
            audioMerAdapter.submitList(audioMerModel.getListAudio())
        }
        if (audioMerModel.searchAudio(audioMerModel.getListAudio(), yourTextSearch).isNotEmpty()) {
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

        audioMerModel.getCountItemChoose().observe(this.viewLifecycleOwner, Observer {
            if (it >= 2) {
                setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
            } else {
                setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
            }
            binding.tvCountFileMer.text = "${it} file"
        })


    }

    private fun showKeybroad() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
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
    }


    private fun showEmptyList() {
        showProgressBar(false)
        binding.rvMerge.visibility = View.INVISIBLE
        binding.ivEmptyListMerge.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.VISIBLE
    }

    private fun showList() {
        showProgressBar(false)
        binding.rvMerge.visibility = View.VISIBLE
        binding.ivEmptyListMerge.visibility = View.INVISIBLE
        binding.tvEmptyListMer.visibility = View.INVISIBLE
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

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        super.onReceivedAction(fragmentMeta)

        Log.d("TAG", "onReceivedAction: receive data")
        if (fragmentMeta.action.equals("ACTION_DELETE")) {
            val audio = fragmentMeta.data as AudioCutterView
            audioMerAdapter.submitList(audioMerModel.getlistAfterReceive(audio))
            Log.d("TAG", " getlistAfterReceive: data ${audio.audioFile.fileName} ")
        }

    }

    private fun handleAudiofile() {
        ManagerFactory.getDefaultAudioPlayer().stop()

        val listItemHandle = audioMerModel.getListItemChoose()
        val arrayAudio: Array<String> = Array(listItemHandle.size) { "" }
        var index = 0
        for (item in listItemHandle) {
            arrayAudio[index] = item.audioFile.file.absolutePath
            index++
        }
        viewStateManager.onMergingItemClicked(this, arrayAudio)
    }


    private fun previousStatus() {
        binding.edtMerSearch.setText("")
        binding.rvMerge.visibility = View.VISIBLE
        binding.rltNextMerParent.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.GONE
        binding.ivEmptyListMerge.visibility = View.GONE
        audioMerAdapter.submitList(audioMerModel.getListAudio())
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
        ManagerFactory.getDefaultAudioPlayer().stop()
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioMerge.visibility = View.VISIBLE
        } else {
            binding.pgrAudioMerge.visibility = View.GONE
        }
    }
}

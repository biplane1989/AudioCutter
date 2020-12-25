package com.example.audiocutter.functions.audiochooser.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MergeChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.MergeChooserAdapter
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView

class MergeChooserScreen : BaseFragment(), View.OnClickListener, MergeChooserAdapter.AudioMergeListener {
    private lateinit var binding: MergeChooserScreenBinding
    private lateinit var audioMerAdapter: MergeChooserAdapter
    private lateinit var audioMerModel: MergeChooserModel
    var currentPos = -1

    var stateObserver = Observer<Int> {
        when (it) {
            1 -> {
                showProgressBar(true)
            }
            0 -> {
                showProgressBar(false)
            }
            -1 -> {
                showProgressBar(false)
            }
        }
    }

    var stateChecked = Observer<Int> {
        Log.d("TAG", "stateChecked: $it")
        if (it > 1) {
            setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
        } else {
            setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
        }
        binding.tvCountFileMer.text = "$it file"
    }

    @SuppressLint("SetTextI18n")
    private val listAudioObserver = Observer<List<AudioCutterView>?> { listMusic ->
        if (listMusic == null) {
            binding.rvMerge.visibility = View.INVISIBLE
        } else {
            if (listMusic.isEmpty()) {
                showEmptyList()

            } else {

                audioMerAdapter.submitList(ArrayList(listMusic))
                showList()
                showProgressBar(false)

            }
//            var count = 0
//            listMusic.forEach {
//                if (it.isCheckChooseItem) {
//                    count++
//                        if (count >= 2) {
//                            setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
//                        } else {
//                            setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
//                    }
//                }
//            }

        }


    }
    private val emptyState = Observer<Boolean> {
        if (it) {
            showList()
        } else {
            showEmptyList()
        }
    }


    override fun onPause() {
        super.onPause()
        hideKeyboard()
        audioMerModel.pause()
    }

//    private val playerInfoObserver = Observer<PlayerInfo> {
//    audioMerModel.updateMediaInfo(it)
//    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerModel = ViewModelProvider(this).get(MergeChooserModel::class.java)

        audioMerAdapter = MergeChooserAdapter(
            requireContext(),
            audioMerModel.getAudioPlayer(),
            lifecycleScope,
            requireActivity()
        )
//        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
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
            audioMerModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)
            audioMerModel.getAllAudioFile().observe(viewLifecycleOwner, listAudioObserver)
            audioMerModel.getStateEmpty().observe(viewLifecycleOwner, emptyState)
            audioMerModel.getStateChecked().observe(viewLifecycleOwner, stateChecked)

        }
    }


    private fun checkEdtSearchAudio() {
        binding.edtMerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(textChange: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                audioMerModel.stop()
                searchAudioByName(textChange.toString())
                if (textChange.toString() != "") {
                    binding.ivMerScreenClose.visibility = View.VISIBLE
                } else {
                    binding.ivMerScreenClose.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
//        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
//        binding.tvCountFileMer.text = getString(R.string.countFile)
//        showList()
        if (yourTextSearch.isEmpty()) {
            audioMerModel.searchAudio("")
        }
        audioMerModel.searchAudio(yourTextSearch)
    }


    private fun initViews() {

        binding.rltNextMer.isEnabled = false
        binding.ivMerScreenSearch.setOnClickListener(this)
        binding.ivMerScreenBackEdt.setOnClickListener(this)
        binding.ivMerScreenClose.setOnClickListener(this)
        binding.rltNextMer.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        audioMerAdapter.setAudioListener(this)

//        searchAudioByName(binding.edtMerSearch.text.toString())

    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtMerSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun hideOrShowEditText(status: Int) {
        binding.ivMerScreenBackEdt.visibility = status
//        binding.ivMerScreenClose.visibility = status
        binding.edtMerSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        binding.ivMerScreenSearch.visibility = status
        binding.tvMerScreen.visibility = status
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
    override fun chooseItemAudio(audioCutterView: AudioCutterView, rs: Boolean) {
        var count = 0

        audioMerModel.chooseItemAudioFile(audioCutterView, rs)
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
        binding.rltNextMer.background = (ContextCompat.getDrawable(requireContext(), bg))
        binding.ivNextMer.setColorFilter(ContextCompat.getColor(requireContext(), color))
        binding.tvNextMer.setTextColor(ContextCompat.getColor(requireContext(), color))
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

      if(audioMerModel.getAudioPlayer().getAudioIsPlaying()){
          audioMerModel.stop()
      }
        val listItemHandle = audioMerModel.getListItemChoose()

        val arrayAudio: Array<String> = Array(listItemHandle.size) { "" }
        var index = 0
        for (item in listItemHandle) {
            arrayAudio[index] = item.audioFile.file.absolutePath
            index++
        }
        previousStatus()
        viewStateManager.onMergingItemClicked(this, arrayAudio)


    }


    private fun previousStatus() {
        binding.edtMerSearch.setText("")
        binding.rvMerge.visibility = View.VISIBLE
        binding.rvMerge.scrollToPosition(0)
        binding.rltNextMerParent.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.INVISIBLE
        binding.ivEmptyListMerge.visibility = View.INVISIBLE
        //audioMerAdapter.submitList(audioMerModel.getListAudio())
        hideKeyboard()
        hideOrShowEditText(View.INVISIBLE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.INVISIBLE)
        showKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getDefaultAudioPlayer().stop()
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioMerge.visibility = View.VISIBLE
        } else {
            binding.pgrAudioMerge.visibility = View.INVISIBLE
        }
    }

    override fun getFragmentViewModel(): IViewModel? {
        return audioMerModel
    }
}

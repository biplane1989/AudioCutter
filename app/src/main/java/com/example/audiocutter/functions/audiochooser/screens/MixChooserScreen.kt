package com.example.audiocutter.functions.audiochooser.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MixChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.MixChooserAdapter
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView

class MixChooserScreen : BaseFragment(), View.OnClickListener, MixChooserAdapter.AudioMixerListener {

    val TAG = CutChooserScreen::class.java.name
    private lateinit var audioMixAdapter: MixChooserAdapter
    private lateinit var audioMixModel: MixChooserModel
    private lateinit var binding: MixChooserScreenBinding
    private var currentPos = -1
    private var stateObserver = Observer<Int> {
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

    private val listAudioObserver = Observer<List<AudioCutterView>?> { listMusic ->

        if (listMusic == null) {
            binding.rvMixer.visibility = View.INVISIBLE
        } else {
            if (listMusic.isEmpty()) {
                showEmptyList()
            } else {

                audioMixAdapter.submitList(ArrayList(listMusic))
                showList()
                showProgressBar(false)
            }
//            var count = 0
//            listMusic.forEach {
//                if (it.isCheckChooseItem) {
//                    count++
//                    if (count >= 2) {
//                        setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
//                    } else {
//                        setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
//                    }
//                }
//            }
//            binding.tvCountFile.text = "${count} file"
        }
    }
    private val emptyState = Observer<Boolean> {
        if (it) {
            showList()
        } else {
            showEmptyList()
        }
    }

    private val isChoseItemObserver = Observer<Boolean> {
        if (it) {
            showToast(getString(R.string.ToastExceed))
        }
    }

    var stateChecked = Observer<Int> {
        if (it > 1) {
            setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
        } else {
            setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
        }
        binding.tvCountFile.text = "$it file"
    }


    private fun showList() {
        binding.rvMixer.visibility = View.VISIBLE
        binding.ivEmptyListMixer.visibility = View.INVISIBLE
        binding.tvEmptyListMixer.visibility = View.INVISIBLE
    }

//    private val playerInfoObserver = Observer<PlayerInfo> {
//        audioMixModel.updateMediaInfo(it)
//    }


    override fun onPause() {
        super.onPause()
        hideKeyboard()
        audioMixModel.pause()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMixModel = ViewModelProvider(this).get(MixChooserModel::class.java)
        audioMixAdapter = MixChooserAdapter(requireContext(), audioMixModel.getAudioPlayer(), lifecycleScope)
//        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mix_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val listAudioViewLiveData = audioMixModel.getAllAudioFile()
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
            audioMixModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)
            audioMixModel.getStateEmpty().observe(viewLifecycleOwner, emptyState)
            audioMixModel.getStateChecked().observe(viewLifecycleOwner, stateChecked)
            audioMixModel.getIsChooseItemState().observe(viewLifecycleOwner, isChoseItemObserver)

        }
    }

    private fun showEmptyList() {
        binding.rvMixer.visibility = View.INVISIBLE
        binding.ivEmptyListMixer.visibility = View.VISIBLE
        binding.tvEmptyListMixer.visibility = View.VISIBLE
        showProgressBar(false)
    }


    private fun checkEdtSearchAudio() {
        binding.edtMixerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(textChange: CharSequence?, p1: Int, p2: Int, p3: Int) {
                audioMixModel.stop()
                searchAudioByName(textChange.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        binding.tvCountFile.text = getString(R.string.countFile)
//        showList()
        if (yourTextSearch.isEmpty()) {
            audioMixModel.searchAudio("")
        }
        audioMixModel.searchAudio(yourTextSearch)
    }


    private fun initViews() {

        binding.ivMixerScreenSearch.setOnClickListener(this)
        binding.ivMixerScreenBackEdt.setOnClickListener(this)
        binding.ivMixerScreenClose.setOnClickListener(this)
        binding.rltNextMixer.setOnClickListener(this)
        binding.ivMixerScreenBack.setOnClickListener(this)
        audioMixAdapter.setAudioCutterListtener(this)


    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtMixerSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideOrShowEditText(status: Int) {
        binding.ivMixerScreenBackEdt.visibility = status
        binding.ivMixerScreenClose.visibility = status
        binding.edtMixerSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        binding.ivMixerScreenSearch.visibility = status
        binding.tbNameMixer.visibility = status
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioMix.visibility = View.VISIBLE
        } else {
            binding.pgrAudioMix.visibility = View.INVISIBLE
        }
    }


    private fun initLists() {


        binding.rvMixer.adapter = audioMixAdapter
        binding.rvMixer.setHasFixedSize(true)
        binding.rvMixer.layoutManager = LinearLayoutManager(requireContext())

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
    override fun chooseItemAudio(itemAudio: AudioCutterView, rs: Boolean) {
        audioMixModel.chooseItemAudioFile(itemAudio, rs)

//        if (audioMixModel.isChooseItem) {
//            showToast(getString(R.string.ToastExceed))
//        }
    }


    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        binding.rltNextMixer.isEnabled = rs
        binding.rltNextMixer.background = ContextCompat.getDrawable(requireContext(), bg)
        binding.ivNextMixer.setColorFilter((ContextCompat.getColor(requireContext(), color)))
        binding.tvNextMixer.setTextColor((ContextCompat.getColor(requireContext(), color)))
    }


    override fun onClick(view: View) {
        when (view) {
            binding.ivMixerScreenSearch -> {
                searchAudiofile()
            }
            binding.ivMixerScreenBackEdt -> {
                previousStatus()
            }
            binding.ivMixerScreenClose -> {
                clearText()
            }
            binding.rltNextMixer -> {
                handleAudiofile()
            }
            binding.ivMixerScreenBack -> {
                activity?.onBackPressed()
            }

        }
    }

    private fun handleAudiofile() {
        val listItemHandle = audioMixModel.getListItemChoose()
        if (listItemHandle.size == 2) {
            viewStateManager.mixingOnSelected(this, listItemHandle[0].audioFile, listItemHandle[1].audioFile)
        }
        /* listItemHandle.forEach {
             Log.d(TAG, "handleAudiofile: ${it.audioFile.fileName}")
         }*/


        /**place handle listItem choose*/

    }

    private fun clearText() {
        if (!binding.edtMixerSearch.text.toString().isEmpty()) {
            binding.edtMixerSearch.setText("")
        }
    }

    private fun previousStatus() {
        binding.edtMixerSearch.setText("")
        binding.rvMixer.visibility = View.VISIBLE
        binding.rltNextMixerParent.visibility = View.VISIBLE
        binding.tvEmptyListMixer.visibility = View.INVISIBLE
        binding.ivEmptyListMixer.visibility = View.INVISIBLE
//        audioMixAdapter.submitList(audioMixModel.getListAudio())
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

}





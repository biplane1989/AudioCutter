package com.example.audiocutter.functions.audiochooser.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
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
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MixChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.adapters.MixChooserAdapter

class MixChooserScreen : BaseFragment(), View.OnClickListener,
    MixChooserAdapter.AudioMixerListener {

    val TAG = CutChooserScreen::class.java.name
    private lateinit var audioMixAdapter: MixChooserAdapter
    private lateinit var audioMixModel: MixChooserModel
    private lateinit var binding: MixChooserScreenBinding
    var currentPos = -1

    var listTmp: MutableList<AudioCutterView> = mutableListOf()

    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        listTmp = listMusic.toMutableList()
        audioMixAdapter.submitList(ArrayList(listMusic))
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioMixAdapter.submitList(audioMixModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMixAdapter =
            MixChooserAdapter(
                requireContext()
            )
        audioMixModel = ViewModelProvider(this).get(MixChooserModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            listAudioViewLiveData.removeObserver(listAudioObserver)
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
        }
    }

    private fun checkEdtSearchAudio() {
        binding.edtMixerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: $p0 - $p1-  $p2- $p3")
                searchAudioByName(binding.edtMixerSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        setColorButtonNext(R.color.colorBlack, R.drawable.bg_next_audio_disabled, false)
        binding.tvCountFile.text = getString(R.string.countFile)
        binding.rvMixer.visibility = View.VISIBLE
        binding.rltNextMixerParent.visibility = View.VISIBLE
        binding.tvEmptyListMixer.visibility = View.GONE
        binding.ivEmptyListMixer.visibility = View.GONE
        if (yourTextSearch.isEmpty()) {
            audioMixAdapter.submitList(listTmp)
        }
        if (audioMixModel.searchAudio(listTmp, yourTextSearch).isNotEmpty()) {
            audioMixAdapter.submitList(audioMixModel.getListsearch())
        } else {
            binding.rvMixer.visibility = View.GONE
            binding.rltNextMixerParent.visibility = View.GONE
            binding.tvEmptyListMixer.visibility = View.VISIBLE
            binding.ivEmptyListMixer.visibility = View.VISIBLE
        }
    }


    private fun initViews() {
        binding.ivAudioMixerScreenFile.setOnClickListener(this)
        binding.ivMixerScreenSearch.setOnClickListener(this)
        binding.ivMixerScreenBackEdt.setOnClickListener(this)
        binding.ivMixerScreenClose.setOnClickListener(this)
        binding.rltNextMixer.setOnClickListener(this)
        binding.ivMixerScreenBack.setOnClickListener(this)
        audioMixAdapter.setAudioCutterListtener(this)
    }

    private fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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
        binding.ivAudioMixerScreenFile.visibility = status
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
        binding.tvCountFile.text = "${audioMixModel.checkList()} file"
    }


    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        binding.rltNextMixer.isEnabled = rs
        binding.rltNextMixer.setBackgroundResource(bg)
        binding.ivNextMixer.setColorFilter(requireActivity().resources.getColor(color));
        binding.tvNextMixer.setTextColor(requireActivity().resources.getColor(color))
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

        listItemHandle.forEach {
            Log.d(TAG, "handleAudiofile: ${it.audioFile.fileName}")
        }


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
        binding.tvEmptyListMixer.visibility = View.GONE
        binding.ivEmptyListMixer.visibility = View.GONE
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




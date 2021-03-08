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
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MixChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.FolderCutChooserAdapter
import com.example.audiocutter.functions.audiochooser.adapters.MixChooserAdapter
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.objects.FolderItem
import com.example.audiocutter.functions.audiochooser.objects.FolderStatus
import com.example.audiocutter.functions.common.SortAudioPopupWindow
import com.google.android.material.snackbar.Snackbar

class MixChooserScreen : BaseFragment(), View.OnClickListener,
    MixChooserAdapter.AudioMixerListener {

    val TAG = CutChooserScreen::class.java.name
    private lateinit var audioMixAdapter: MixChooserAdapter
    private lateinit var audioMixModel: MixChooserModel
    private lateinit var binding: MixChooserScreenBinding
    private var isSearchStatus = false

    private val folderAdapter: FolderCutChooserAdapter by lazy {
        FolderCutChooserAdapter(this::clickFolderItem)
    }

    private val folderObserver = Observer<List<FolderItem>> {
        folderAdapter.submitList(it)
        Log.d(TAG, "folder adapter : list folder: size : " + it.size)
    }

    //    private var toast: Toast? = null
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


    private val listAudioObserver = Observer<List<AudioCutterViewItem>?> { listMusic ->

        if (listMusic == null) {
            binding.rvMixer.visibility = View.INVISIBLE
        } else {
            if (listMusic.isEmpty()) {
//                showEmptyList()
            } else {
                audioMixAdapter.submitList(ArrayList(listMusic)) {
                    Log.d(TAG, "submitList done : ")
//                    if (isSearchStatus) {
//                        binding.rvMixer.scrollToPosition(0)
//                    }
                }
//                showList()
                showProgressBar(false)
            }
        }
    }
    private val emptyState = Observer<Boolean> {
        if (!it) {
            showList()
        } else {
            showEmptyList()
        }
    }

    private val folderStatusObserver = Observer<FolderStatus>{
        Log.d(TAG, "status: ssss")
        if (it.status){
            binding.rvMixer.visibility = View.INVISIBLE
            audioMixAdapter.submitList(emptyList())
            binding.rvFolderMixer.visibility = View.VISIBLE
            Log.d(TAG, "status true : $it")
        }else{
            binding.rvFolderMixer.visibility = View.INVISIBLE
            binding.rvMixer.visibility = View.VISIBLE
            Log.d(TAG, "status false: $it")
        }
    }

    private fun showList() {
        if (audioMixModel.folderLiveData.value?.status == true){
            binding.rvFolderMixer.visibility = View.VISIBLE
        }else{
            binding.rvMixer.visibility = View.VISIBLE
        }
        binding.ivEmptyListMixer.visibility = View.INVISIBLE
        binding.tvEmptyListMixer.visibility = View.INVISIBLE
    }


    override fun onPause() {
        super.onPause()
        hideKeyboard()
        audioMixModel.pause()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMixModel = ViewModelProvider(this).get(MixChooserModel::class.java)
        audioMixAdapter = MixChooserAdapter(
            requireContext(),
            audioMixModel.getAudioPlayer(),
            lifecycleScope,
            requireActivity()
        )

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
        observerData()
    }

    private fun observerData() {
        audioMixModel.listAudioCutterViewItems.observe(viewLifecycleOwner, listAudioObserver)
        audioMixModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)
        audioMixModel.isEmptyState.observe(viewLifecycleOwner, emptyState)
        audioMixModel.folderLiveData.observe(viewLifecycleOwner,folderStatusObserver)

        audioMixModel.checkNextButtonEnable.observe(viewLifecycleOwner) {
            if (it) {
                setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
            } else {
                setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
            }
        }
        audioMixModel.countItemSelected.observe(viewLifecycleOwner) {
            binding.tvCountFile.text = "$it ${getString(R.string.merger_screen_file)}"
        }

        audioMixModel.checkMoreThanTwoItemsIsSelected.observe(viewLifecycleOwner) {
            if (it) {
                showNotification(getString(R.string.ToastExceed))
            }
        }
        audioMixModel.onMixingNextButtonClicked.observe(viewLifecycleOwner) {
            previousStatus()
            viewStateManager.mixingOnSelected(this, it[0], it[1])
        }
        audioMixModel.showSortAudioDialog.observe(viewLifecycleOwner) {
            val sortAudioPopupWindow = SortAudioPopupWindow(
                binding.ivMixScreenSort, it
            ) {
                audioMixModel.sortAudioBy(it)
            }
            sortAudioPopupWindow.show()
        }

        audioMixModel.listFolder.observe(viewLifecycleOwner, folderObserver)

    }

    private fun showEmptyList() {
        binding.rvMixer.visibility = View.INVISIBLE
        binding.rvFolderMixer.visibility = View.INVISIBLE
        binding.ivEmptyListMixer.visibility = View.VISIBLE
        binding.tvEmptyListMixer.visibility = View.VISIBLE
        showProgressBar(false)
    }

    private fun clickFolderItem(folderItem: FolderItem) {        //click folder item todo
        audioMixModel.clickItemFolder(folderItem)

//        binding.rvFolderCutter.visibility = View.INVISIBLE
//        binding.rvAudioCutter.visibility = View.VISIBLE
    }

    private fun checkEdtSearchAudio() {
        binding.edtMixerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(textChange: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isSearchStatus) {
                    binding.rvMixer.post {
                        binding.rvMixer.scrollToPosition(0)
                    }
                }
                audioMixModel.stop()
                searchAudioByName(textChange.toString())
                if (textChange.toString() != "") {
                    binding.ivMixerScreenClose.visibility = View.VISIBLE
                } else {
                    binding.ivMixerScreenClose.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
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
        binding.ivMixScreenSort.setOnClickListener(this)
        binding.tbNameMixer.setOnClickListener(this)

    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
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
        binding.edtMixerSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        binding.ivMixerScreenSearch.visibility = status
        binding.tbNameMixer.visibility = status
        binding.ivMixScreenSort.visibility = status
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

        binding.rvFolderMixer.adapter = folderAdapter
        binding.rvFolderMixer.setHasFixedSize(true)
        binding.rvFolderMixer.layoutManager = LinearLayoutManager(requireContext())
    }

    @SuppressLint("SetTextI18n")
    override fun selectItem(position: Int) {
        audioMixModel.chooseItemAudioFile(position)
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
                isSearchStatus = true
            }
            binding.ivMixerScreenBackEdt -> {
                previousStatus()
                isSearchStatus = false
            }
            binding.ivMixerScreenClose -> {
                clearText()
            }
            binding.rltNextMixer -> {
                audioMixModel.clickedOnNextButton()
            }
            binding.ivMixerScreenBack -> {
                activity?.onBackPressed()
            }
            binding.ivMixScreenSort -> {
                audioMixModel.clickedOnSortButton()
            }

            binding.tbNameMixer ->{
                audioMixModel.showFolder()
            }
        }
    }

    private fun clearText() {
        if (!binding.edtMixerSearch.text.toString().isEmpty()) {
            binding.edtMixerSearch.setText("")
        }
    }

    private fun previousStatus() {
        binding.edtMixerSearch.setText("")
        binding.rvMixer.visibility = View.VISIBLE
        binding.rvMixer.scrollToPosition(0)
        binding.rltNextMixerParent.visibility = View.VISIBLE
        binding.tvEmptyListMixer.visibility = View.INVISIBLE
        binding.ivEmptyListMixer.visibility = View.INVISIBLE
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





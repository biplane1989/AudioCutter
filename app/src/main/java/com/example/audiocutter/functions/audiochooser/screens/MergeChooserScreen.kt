package com.example.audiocutter.functions.audiochooser.screens

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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MergeChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.FolderCutChooserAdapter
import com.example.audiocutter.functions.audiochooser.adapters.MergeChooserAdapter
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.objects.FolderItem
import com.example.audiocutter.functions.audiochooser.objects.FolderStatus
import com.example.audiocutter.functions.common.SortAudioPopupWindow
import com.google.android.material.snackbar.Snackbar

class MergeChooserScreen : BaseFragment(), View.OnClickListener, MergeChooserAdapter.AudioMergeListener {

    val TAG = "tag"
    private var positionRv = 0
    private lateinit var binding: MergeChooserScreenBinding
    private lateinit var audioMerAdapter: MergeChooserAdapter
    private val audioMerModel: MergeChooserModel by navGraphViewModels(R.id.mer_navigation)
    private var isSearchStatus = false
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

    var countItemSelected = Observer<Int> {
        binding.tvCountFileMer.text = "$it ${getString(R.string.merger_screen_file)}"
    }

    private val listAudioObserver = Observer<List<AudioCutterViewItem>?> { listMusic ->
        if (listMusic == null) {
            binding.rvMerge.visibility = View.INVISIBLE
        } else {
            if (listMusic.isEmpty()) {
//                showEmptyList()

            } else {
                audioMerAdapter.submitList(listMusic)
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

    private val folderAdapter: FolderCutChooserAdapter by lazy {
        FolderCutChooserAdapter(this::clickFolderItem)
    }

    private val folderObserver = Observer<List<FolderItem>> {
        folderAdapter.submitList(it)
        Log.d("TAG", "folder adapter : list folder: size : " + it.size)
    }

    private val folderStatusObserver = Observer<FolderStatus>{
        Log.d(TAG, "status: ssss")
        if (it.status){
            binding.rvMerge.visibility = View.INVISIBLE
            audioMerAdapter.submitList(emptyList())
            binding.rvFolderMerge.visibility = View.VISIBLE
            binding.ivMerScreenSearch.visibility = View.INVISIBLE
            Log.d(TAG, "status true : $it")
        }else{
            binding.rvFolderMerge.visibility = View.INVISIBLE
            binding.rvMerge.visibility = View.VISIBLE
            binding.ivMerScreenSearch.visibility = View.VISIBLE
            Log.d(TAG, "status false: $it")
        }
        binding.tvMerScreen.text = it.folder
    }

    private val adapterObserver = object: RecyclerView.AdapterDataObserver(){
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            binding.rvMerge.scrollToPosition(0)
        }
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            binding.rvMerge.scrollToPosition(0)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            binding.rvMerge.scrollToPosition(0)
        }
    }


    override fun onPause() {
        super.onPause()
        hideKeyboard()
        audioMerModel.pause()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        audioMerAdapter = MergeChooserAdapter(requireContext(), audioMerModel.getAudioPlayer(), lifecycleScope, requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.merge_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()

        binding.rvMerge.scrollToPosition(positionRv)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        observerData()
    }

    private fun observerData() {
        audioMerModel.stateLoadProgress.observe(viewLifecycleOwner, stateObserver)
        audioMerModel.listAudioCutterViewItems.observe(viewLifecycleOwner, listAudioObserver)
        audioMerModel.isEmptyState.observe(viewLifecycleOwner, emptyState)

        audioMerModel.countItemSelected.observe(viewLifecycleOwner, countItemSelected)

        audioMerModel.folderLiveData.observe(viewLifecycleOwner,folderStatusObserver)
        audioMerModel.listFolder.observe(viewLifecycleOwner, folderObserver)

        audioMerModel.checkNextButtonEnable.observe(viewLifecycleOwner) {
            if (it) {
                setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
            } else {
                setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
            }
        }
        audioMerModel.onMergingNextButtonClicked.observe(viewLifecycleOwner) {
            if (it) {
                previousStatus()
                viewStateManager.onMergingItemClicked(this)
            }

        }
        audioMerModel.checkLessThanTwoItemsIsSelected.observe(viewLifecycleOwner) {
            if (it) {
                showNotification(getString(R.string.rule_amout_item_mer))
            }
        }

        audioMerModel.showSortAudioDialog.observe(viewLifecycleOwner) {
            val sortAudioPopupWindow = SortAudioPopupWindow(binding.ivMerScreenSort, it) {
                audioMerModel.sortAudioBy(it)
            }
            sortAudioPopupWindow.show()
        }
    }

    private fun checkEdtSearchAudio() {
        binding.edtMerSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(textChange: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                if (isSearchStatus) {
//                    binding.rvMerge.post {
//                        binding.rvMerge.smoothScrollToPosition(0)
//                    }
//                }
                audioMerModel.stop()
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

        if (yourTextSearch.isEmpty()) {
            audioMerModel.searchAudio("")
        }
        audioMerModel.searchAudio(yourTextSearch)
    }

    private fun clickFolderItem(folderItem: FolderItem) {        //click folder item todo
        audioMerModel.clickItemFolder(folderItem)

    }

    private fun initViews() {

        binding.rltNextMer.isEnabled = false
        binding.ivMerScreenSearch.setOnClickListener(this)
        binding.ivMerScreenBackEdt.setOnClickListener(this)
        binding.ivMerScreenClose.setOnClickListener(this)
        binding.rltNextMer.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        audioMerAdapter.setAudioListener(this)
        binding.ivMerScreenSort.setOnClickListener(this)
        binding.linear.setOnClickListener(this)

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
        binding.edtMerSearch.visibility = status

    }

    private fun hideOrShowView(status: Int) {
        binding.ivMerScreenSearch.visibility = status
        binding.tvMerScreen.visibility = status
        binding.ivMerScreenSort.visibility = status
//        binding.ivAudioMerScreenFile.visibility = status
    }

    private fun initLists() {

        binding.rvMerge.adapter = audioMerAdapter
        binding.rvMerge.setHasFixedSize(true)
        binding.rvMerge.layoutManager = LinearLayoutManager(requireContext())

        binding.rvFolderMerge.adapter = folderAdapter
        binding.rvFolderMerge.setHasFixedSize(true)
        binding.rvFolderMerge.layoutManager = LinearLayoutManager(requireContext())

        audioMerAdapter.registerAdapterDataObserver(adapterObserver)

        binding.rvMerge.addOnScrollListener(object : RecyclerView.OnScrollListener() {      // su kien luu lai gia tri scroll
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                positionRv = binding.rvMerge.computeVerticalScrollOffset()
                Log.d(TAG, "onScrolled: positionRv: "+ positionRv)
            }
        })
    }

    override fun chooseItemAudio(position: Int) {
        audioMerModel.chooseItemAudioFile(position)
    }

    private fun showEmptyList() {
        showProgressBar(false)
        binding.rvFolderMerge.visibility = View.INVISIBLE
        binding.rvMerge.visibility = View.INVISIBLE
        binding.ivEmptyListMerge.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.VISIBLE
    }

    private fun showList() {
        if (audioMerModel.folderLiveData.value?.status == true){
            binding.rvFolderMerge.visibility = View.VISIBLE
        }else{
            binding.rvMerge.visibility = View.VISIBLE
        }
        showProgressBar(false)

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
                isSearchStatus = true
            }
            binding.ivMerScreenBackEdt -> {
                previousStatus()
                isSearchStatus = false
            }
            binding.ivMerScreenClose -> {
                if (!binding.edtMerSearch.text.toString().isEmpty()) {
                    binding.edtMerSearch.setText("")
                }
            }
            binding.rltNextMer -> {
                audioMerModel.clickedOnNextButton()
            }
            binding.ivMerScreenBack -> {
                activity?.onBackPressed()
            }
            binding.ivMerScreenSort -> {
                audioMerModel.clickedOnSortButton()
            }
            binding.linear ->{
                audioMerModel.showFolder()
            }
        }
    }


    private fun previousStatus() {
        binding.edtMerSearch.setText("")
        binding.rvMerge.visibility = View.VISIBLE
        //binding.rvMerge.scrollToPosition(0)
        binding.rltNextMerParent.visibility = View.VISIBLE
        binding.tvEmptyListMer.visibility = View.INVISIBLE
        binding.ivEmptyListMerge.visibility = View.INVISIBLE
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
        audioMerAdapter.unregisterAdapterDataObserver(adapterObserver)
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioMerge.visibility = View.VISIBLE
            binding.linear.visibility = View.INVISIBLE
        } else {
            binding.pgrAudioMerge.visibility = View.INVISIBLE
            binding.linear.visibility = View.VISIBLE
        }
    }

    override fun getFragmentViewModel(): IViewModel? {
        return audioMerModel
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }
}

package com.example.audiocutter.functions.contacts.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.ListContactSelectScreenBinding
import com.example.audiocutter.functions.common.SortAudioPopupWindow
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.contacts.adapters.SelectAudioScreenCallback
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.util.FileUtils
import com.google.android.material.snackbar.Snackbar


class ListSelectAudioScreen : BaseFragment(), SelectAudioScreenCallback, View.OnClickListener {
    private val TAG = "giangtd"
    private val safeArg: ListSelectAudioScreenArgs by navArgs()
    private lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    private lateinit var mListSelectAdapter: ListSelectAdapter
    private val REQ_CODE_PICK_SOUNDFILE = 1990
    private var positionSelect = -1
    private lateinit var binding: ListContactSelectScreenBinding
    private var fileUri = ""
    private var isSearchStatus = false

    // observer data
    private val listAudioObserver = Observer<List<SelectItemView>> { listAudio ->
        if (listAudio != null) {
            mListSelectAdapter.submitList(ArrayList(listAudio))
//            if (isSearchStatus) {
//                binding.rvListSelectAudio.post {
//                    binding.rvListSelectAudio.smoothScrollToPosition(0)
//                }
//            }
        }
    }

    private val adapterObserver = object: RecyclerView.AdapterDataObserver(){
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            binding.rvListSelectAudio.scrollToPosition(0)
        }
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            binding.rvListSelectAudio.scrollToPosition(0)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            binding.rvListSelectAudio.scrollToPosition(0)
        }
    }

    // observer loading status
    private val loadingStatusObserver = Observer<Boolean> {
        if (it) {
            binding.pbSelect.visibility = View.VISIBLE
        } else {
            binding.pbSelect.visibility = View.GONE
            binding.rvListSelectAudio.scrollToPosition(mListSelectAudioViewModel.getIndexSelectRingtone(safeArg.uri))       // set vi tri khi chuyen sang man hinh den bai nhac da duoc chon
        }
    }

    // observer is empty status
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
            binding.rvListSelectAudio.visibility = View.INVISIBLE
            binding.clBottom.visibility = View.GONE
            binding.clNoAudio.visibility = View.VISIBLE
        } else {
            binding.rvListSelectAudio.visibility = View.VISIBLE
            binding.clBottom.visibility = View.VISIBLE
            binding.clNoAudio.visibility = View.INVISIBLE
        }
    }

    private val isChoseRingTone = Observer<Boolean> {
        if (!it) {
            binding.btnSave.visibility = View.INVISIBLE
            binding.tvSave.visibility = View.INVISIBLE

            binding.btnNotSave.visibility = View.VISIBLE
            binding.tvNotSave.visibility = View.VISIBLE
        } else {
            binding.btnSave.visibility = View.VISIBLE
            binding.tvSave.visibility = View.VISIBLE

            binding.btnNotSave.visibility = View.INVISIBLE
            binding.tvNotSave.visibility = View.INVISIBLE
        }
    }

    private fun init() {
        binding.rvListSelectAudio.layoutManager = LinearLayoutManager(context)
        //binding.rvListSelectAudio.setHasFixedSize(true)
        binding.rvListSelectAudio.adapter = mListSelectAdapter
        mListSelectAdapter.registerAdapterDataObserver(adapterObserver)
    }

    private fun registerObservers() {
        mListSelectAudioViewModel.getListAudioFile().observe(viewLifecycleOwner, listAudioObserver)

        mListSelectAudioViewModel.getIsEmptyStatus()
            .observe(viewLifecycleOwner, isEmptyStatusObserver)
        mListSelectAudioViewModel.getLoadingStatus()
            .observe(viewLifecycleOwner, loadingStatusObserver)
        mListSelectAudioViewModel.getChoseMusic().observe(viewLifecycleOwner, isChoseRingTone)

        mListSelectAudioViewModel.showSortAudioDialog.observe(viewLifecycleOwner) {
            val sortAudioPopupWindow = SortAudioPopupWindow(binding.ivSort, it) {
                mListSelectAudioViewModel.sortAudioBy(it)
            }
            sortAudioPopupWindow.show()
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {       // khi ket thuc animation chuyen man hinh thi moi cho dang ky observe
        if (nextAnim != 0x0) {
            val animator = AnimationUtils.loadAnimation(activity, nextAnim)

            animator.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (enter) {
                        registerObservers()
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {

                }
            })

            return animator
        } else {
            registerObservers()
        }
        return null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_contact_select_screen, container, false)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mListSelectAudioViewModel = ViewModelProviders.of(this)
            .get(ListSelectAudioViewModel::class.java)

        mListSelectAdapter = ListSelectAdapter(this, mListSelectAudioViewModel.getAudioPlayer(), lifecycleScope)
        fileUri = safeArg.uri
        mListSelectAudioViewModel.init(fileUri)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.ivSort.setOnClickListener(this)
        binding.ivFile.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(textChange: CharSequence, start: Int, before: Int, count: Int) {
//                if (isSearchStatus) {
//                    binding.rvListSelectAudio.post {
//                        binding.rvListSelectAudio.smoothScrollToPosition(0)
//                    }
//                }
                mListSelectAudioViewModel.searchAudioFile(textChange.toString())
                if (textChange.toString() != "") {
                    binding.ivClear.visibility = View.VISIBLE

                } else {
                    binding.ivClear.visibility = View.INVISIBLE
                }
            }
        })
    }


    override fun isShowPlayingAudio(filePath: String) {
        positionSelect = 1
        mListSelectAudioViewModel.showPlayingAudio(filePath)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivSearch -> {
                showKeyboard()
                binding.clDefault.visibility = View.INVISIBLE
                binding.clSearch.visibility = View.VISIBLE
                isSearchStatus = true
            }
            binding.ivSearchClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clSearch.visibility = View.INVISIBLE
                binding.edtSearch.text.clear()
                hideKeyboard()
                isSearchStatus = false
            }
            binding.ivClear -> {
                binding.edtSearch.text.clear()
            }
            binding.btnSave -> {
                if (mListSelectAudioViewModel.setRingtone(safeArg.phoneNumber)) {
                    context?.let {
                        showNotification(getString(R.string.result_screen_set_ringtone_successful))
                    }
                } else {
                    context?.let {
                        showNotification(getString(R.string.result_screen_set_ringtone_fail))
                    }
                }
                requireActivity().onBackPressed()
            }
            binding.ivFile -> {
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                }/* else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                } */
                else {
                    intent = Intent(Intent.ACTION_GET_CONTENT)
                }
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.type = "audio/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(Intent.createChooser(intent, getString(R.string.list_select_audio_screen_open_file_title)), REQ_CODE_PICK_SOUNDFILE)
            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
            binding.ivSort -> {
                mListSelectAudioViewModel.clickedOnSortButton()
            }
        }
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK && intent != null) {

            intent.data?.let {

                val path = FileUtils.getUriPath(requireContext(), it)
                path?.let {
                    if (mListSelectAudioViewModel.setRingtoneWithUri(safeArg.phoneNumber, path)) {
                        context?.let {
                            showNotification(getString(R.string.result_screen_set_ringtone_successful))
                        }
                        lifecycleScope.launchWhenResumed {
                            requireActivity().onBackPressed()
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_set_ringtone_fail))
                        }
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onPause() {
        super.onPause()
        if (positionSelect > -1) {
            mListSelectAudioViewModel.stopAudio(positionSelect)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mListSelectAdapter.unregisterAdapterDataObserver(adapterObserver)
    }
}
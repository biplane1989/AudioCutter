package com.example.audiocutter.functions.flashcall.sreens

import android.content.Context
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
import com.example.audiocutter.core.manager.AppFlashItem
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.AppChooserScreenBinding
import com.example.audiocutter.functions.flashcall.adapters.AppFlashAdapter

class AppChooserScreen : BaseFragment(), View.OnClickListener, AppFlashAdapter.AppFlashListener {
    val TAG = "NOCMT"
    private lateinit var binding: AppChooserScreenBinding
    private lateinit var appFlashAdapter: AppFlashAdapter
    private lateinit var appFlashModel: AppFlashModel


    private var listAppObserver = Observer<List<AppFlashItem>?> { listData ->
        if (listData == null) {
            binding.rvAppChooserFlashcall.visibility = View.INVISIBLE
        } else {
            if (listData.isEmpty()) {
                showEmptyList()
            } else {
                appFlashAdapter.submitList(ArrayList(listData))
                showList()
                showProgress(false)
            }
        }
    }

    var stateObserver = Observer<Boolean> {
        when (it) {
            true -> {
                showProgress(true)
            }
            false -> {
                showProgress(false)
            }
        }
    }

    private val emptyState = Observer<Boolean> {
        if (it) {
            showList()
        } else {
            showEmptyList()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.app_chooser_screen, container, false)
        initViews()
        binding.edtAppsSearch.post {
            checkEdtSearchApps()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        appFlashModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)
        appFlashModel.getStateEmpty().observe(viewLifecycleOwner, emptyState)
        appFlashModel.getListApps().observe(viewLifecycleOwner, listAppObserver)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        appFlashAdapter = AppFlashAdapter(requireContext())
        appFlashAdapter.setOnCallBack(this)
        appFlashModel = ViewModelProvider(this).get(AppFlashModel::class.java)
//        appFlashModel.getListApps().observe(this, listAppObserver)
    }


    private fun initViews() {
        binding.ivAppScreenBack.setOnClickListener(this)
        binding.ivAppsScreenSearch.setOnClickListener {
            searchAudiofile()
        }
        binding.ivAppsScreenBackEdt.setOnClickListener {
            previousStatus()
        }
        binding.ivAppsScreenClose.setOnClickListener {
            clearText()
        }
    }


    private fun checkEdtSearchApps() {
        binding.edtAppsSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchAudioByName(binding.edtAppsSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun searchAudioByName(yourTextSearch: String) {
        showList()
        if (yourTextSearch.isEmpty()) {
            appFlashModel.searchApps("")
        }
        appFlashModel.searchApps(yourTextSearch)
    }

    private fun showEmptyList() {
        showProgress(false)
        binding.rvAppChooserFlashcall.visibility = View.INVISIBLE
        binding.lnEmptyApps.visibility = View.VISIBLE
    }

    private fun showList() {
        binding.rvAppChooserFlashcall.visibility = View.VISIBLE
        binding.lnEmptyApps.visibility = View.INVISIBLE
    }


    private fun clearText() {
        if (binding.edtAppsSearch.text.toString().isNotEmpty()) {
            binding.edtAppsSearch.setText("")
        }
    }

    private fun previousStatus() {
        hideKeyboard()
        binding.rvAppChooserFlashcall.visibility = View.VISIBLE
        binding.lnEmptyApps.visibility = View.INVISIBLE
        binding.edtAppsSearch.setText("")
        hideOrShowEditText(View.INVISIBLE)
        hideOrShowView(View.VISIBLE)
    }


    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtAppsSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun searchAudiofile() {
        showKeyboard()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.INVISIBLE)
    }

    private fun hideOrShowView(status: Int) {
        binding.ivAppScreenBack.visibility = status
        binding.ivAppsScreenSearch.visibility = status
        binding.tvAppsScreen.visibility = status

    }

    private fun hideOrShowEditText(status: Int) {
        binding.ivAppsScreenBackEdt.visibility = status
        binding.ivAppsScreenClose.visibility = status
        binding.edtAppsSearch.visibility = status
    }


    private fun initList() {
        binding.rvAppChooserFlashcall.adapter = appFlashAdapter
        binding.rvAppChooserFlashcall.setHasFixedSize(true)
        binding.rvAppChooserFlashcall.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onClick(v: View) {
        when (v) {
            binding.ivAppScreenBack -> {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun showProgress(rs: Boolean) {
        if (rs) {
            binding.pgrAppchoose.visibility = View.VISIBLE
        } else {
            binding.pgrAppchoose.visibility = View.INVISIBLE
        }
    }

    override fun enableFlashForApp(appItem: AppFlashItem) {
        runOnUI {
            ManagerFactory.getFlashCallSetting().enableNotificationFlash(appItem)
        }
    }

    override fun disableFlashForApp(appItem: AppFlashItem) {
        runOnUI {
            ManagerFactory.getFlashCallSetting().disableNotificationFlash(appItem)
        }
    }

}
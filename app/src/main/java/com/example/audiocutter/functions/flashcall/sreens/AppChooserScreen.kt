package com.example.audiocutter.functions.flashcall.sreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.AppFlashItem
import com.example.audiocutter.core.manager.ListAppFlashItemsResult
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.AppChooserScreenBinding
import com.example.audiocutter.functions.flashcall.adapters.AppFlashAdapter

class AppChooserScreen : BaseFragment(), View.OnClickListener, AppFlashAdapter.AppFlashListener {
    private lateinit var binding: AppChooserScreenBinding
    private lateinit var appFlashAdapter: AppFlashAdapter
    private lateinit var appFlashModel: AppFlashModel
    private var listAppObserver = Observer<ListAppFlashItemsResult> {
        if (it.isLoading) {
            showProgress(true)
        } else {
            showProgress(false)
        }

        if (it.data != null) {
            appFlashAdapter.submitList(it.data)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.app_chooser_screen, container, false)
        initList()
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.ivAppScreenBack.setOnClickListener(this)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        appFlashAdapter = AppFlashAdapter(requireContext())
        appFlashAdapter.setOnCallBack(this)
        appFlashModel = ViewModelProvider(this).get(AppFlashModel::class.java)
        appFlashModel.getListData().observe(this, listAppObserver)
    }

    private fun initList() {
        binding.rvAppChooserFlashcall.adapter = appFlashAdapter
        binding.rvAppChooserFlashcall.setHasFixedSize(true)
        binding.rvAppChooserFlashcall.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onClick(v: View) {
        when (v) {
            binding.ivAppScreenBack -> {
                showToast("back Frg")
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
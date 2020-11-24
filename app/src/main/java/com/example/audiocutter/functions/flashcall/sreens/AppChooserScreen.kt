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
import com.example.audiocutter.databinding.AppChooserScreenBinding
import com.example.audiocutter.functions.flashcall.`object`.AppChooserView
import com.example.audiocutter.functions.flashcall.adapters.AppFlashAdapter

class AppChooserScreen : BaseFragment() {
    private lateinit var binding: AppChooserScreenBinding
    private lateinit var appFlashAdapter: AppFlashAdapter
    private lateinit var appFlashModel: AppFlashModel
    var listAppObserver = Observer<List<AppChooserView>?> {
        if (it!!.isNotEmpty()) {
            appFlashAdapter.submitList(it)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.app_chooser_screen, container, false)
        initList()
        return binding.root
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        appFlashAdapter = AppFlashAdapter(requireContext())
        appFlashModel = ViewModelProvider(this).get(AppFlashModel::class.java)
        appFlashModel.getListData(requireContext()).observe(this, listAppObserver)
    }

    private fun initList() {
        binding.rvAppChooserFlashcall.adapter = appFlashAdapter
        binding.rvAppChooserFlashcall.setHasFixedSize(true)
        binding.rvAppChooserFlashcall.layoutManager = LinearLayoutManager(requireContext())
    }

}
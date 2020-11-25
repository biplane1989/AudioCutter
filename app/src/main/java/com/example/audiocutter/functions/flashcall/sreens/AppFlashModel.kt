package com.example.audiocutter.functions.flashcall.sreens

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.functions.flashcall.`object`.AppChooser
import com.example.audiocutter.functions.flashcall.`object`.AppChooserView


class AppFlashModel : BaseViewModel() {


    private val TAG: String = "hhehhe"
    private var _listApp = MutableLiveData<List<AppChooserView>>()
    val listApp: LiveData<List<AppChooserView>>
        get() = _listApp


    @SuppressLint("QueryPermissionsNeeded")
    fun getListData(mContext: Context): LiveData<List<AppChooserView>> {

        return listApp
    }

}
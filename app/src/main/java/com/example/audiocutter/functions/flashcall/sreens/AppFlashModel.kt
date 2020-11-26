package com.example.audiocutter.functions.flashcall.sreens

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.AppFlashItem
import com.example.audiocutter.core.manager.ListAppFlashItemsResult
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope


class AppFlashModel : BaseViewModel() {


    private val TAG: String = "TAG"

    @SuppressLint("QueryPermissionsNeeded")
    fun getListData(): LiveData<ListAppFlashItemsResult> {

        return ManagerFactory.getFlashCallSetting().getListNotificationApps()
    }


}
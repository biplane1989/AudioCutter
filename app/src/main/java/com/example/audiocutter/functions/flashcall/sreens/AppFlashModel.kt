package com.example.audiocutter.functions.flashcall.sreens

import android.util.Log
import androidx.lifecycle.*
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.AppFlashItem
import com.example.audiocutter.core.manager.ManagerFactory
import java.util.*
import kotlin.collections.ArrayList


class AppFlashModel : BaseViewModel() {


    private val TAG: String = "TAG"
    private val _listApps = MediatorLiveData<List<AppFlashItem>?>()

    private var _isEmptyState = MutableLiveData<Boolean>()
    val isEmptyState: LiveData<Boolean>
        get() = _isEmptyState


    private var _stateLoadProgress = MutableLiveData<Boolean>()
    val stateLoadProgress: LiveData<Boolean>
        get() = _stateLoadProgress


    private var filterText = ""

//    @SuppressLint("QueryPermissionsNeeded")
//    fun getListData(): LiveData<ListAppFlashItemsResult> {
//        return ManagerFactory.getFlashCallSetting().getListNotificationApps()
//    }

    init {
        _listApps.addSource(
            ManagerFactory.getFlashCallSetting().getListNotificationApps()
        ) { it ->
            var listAppsTmp: List<AppFlashItem>? = null
            when (it.isLoading) {
                false -> {
                    _stateLoadProgress.postValue(false)
                    val tmpList = ArrayList<AppFlashItem>()
                    it.data!!.forEach {
                        tmpList.add(it)
                    }
                    listAppsTmp = tmpList
                }
                true -> {
                    _stateLoadProgress.postValue(true)
                }

            }
            _listApps.postValue(listAppsTmp)

        }
    }

    private val _listFilteredApps = liveData<List<AppFlashItem>?> {
        emitSource(_listApps.map {
            it?.let {
                var listResult: List<AppFlashItem>? = null
                listResult = ArrayList(it)
                val listEmpty = ArrayList<Boolean>()
                if (filterText.isNotEmpty()) {
                    listResult.clear()
                    it.forEach { item ->
                        val rs =
                            item.name.toLowerCase(Locale.getDefault()).contains(
                                filterText.toLowerCase(Locale.getDefault())
                            )
                        listEmpty.add(rs)
                        if (rs) {
                            listResult.add(item)
                        }
                    }
                    if (!listEmpty.contains(true)) {
                        _isEmptyState.postValue(false)
                    } else {
                        _isEmptyState.postValue(true)
                    }
                }

                listResult
            }
        })
    }

    fun getListApps(): LiveData<List<AppFlashItem>?> {
        return _listFilteredApps
    }

    fun getStateEmpty(): LiveData<Boolean> {
        return isEmptyState
    }

    fun getStateLoading(): LiveData<Boolean> {
        return stateLoadProgress
    }

//    fun getListAllApps(): ArrayList<AppFlashItem> {
//        return ArrayList(_listApps.value ?: ArrayList())
//    }
//
//    fun getListFilteredApps(): ArrayList<AppFlashItem> {
//        return ArrayList(_listFilteredApps.value ?: ArrayList())
//    }

    fun searchApps(yourTextSearch: String) {
        filterText = yourTextSearch
        _listApps.postValue(_listApps.value)
    }


}
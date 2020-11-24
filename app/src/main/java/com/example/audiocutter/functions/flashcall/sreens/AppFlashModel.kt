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
        val pkgManager = mContext.packageManager
        val listTmp = pkgManager.getInstalledApplications(0)
        val installedApps: MutableList<ApplicationInfo> = ArrayList()

        for (app in listTmp) {
            //checks for flags; if flagged, check if updated system app
            when {
                app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 -> {
                    installedApps.add(app)
                }
                app.flags and ApplicationInfo.FLAG_SYSTEM != 0 -> {
                }
                else -> {
                    installedApps.add(app)
                }
            }
        }
        val listApps = ArrayList<AppChooserView>()
        installedApps.forEach {
            listApps.add(AppChooserView(AppChooser(pkgManager.getApplicationLabel(it)
                .toString(), pkgManager.getApplicationIcon(it), it.packageName), true))
        }
        _listApp.postValue(listApps)
        return listApp
    }

}
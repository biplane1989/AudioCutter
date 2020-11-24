package com.example.audiocutter.functions.flashcall.sreens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.functions.flashcall.`object`.AppChooser
import com.example.audiocutter.functions.flashcall.`object`.AppChooserView

class AppFlashModel : BaseViewModel() {


    private var _listApp = MutableLiveData<List<AppChooserView>>()
    val listApp: LiveData<List<AppChooserView>>
        get() = _listApp


    fun getListData(mContext: Context): LiveData<List<AppChooserView>> {
        val bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_call_flash_flcall)
        val bitmap2 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_more_app_share)
        val bitmap3 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_checkdone)
        val bitmap4 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_added_audio)
        val bitmap5 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_audiocutter_back)
        val bitmap6 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_audiocutter_close)
        val bitmap7 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_audiocutter_file)
        val bitmap8 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_setas_done)
        val bitmap9 = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_sound_mixing)
        val name = "facebook"
        val pkg = "com.example.abc"

        val item = AppChooserView(AppChooser(name, bitmap, "dksjdkdjdk"), true)
        val item1 = AppChooserView(AppChooser("dd", bitmap2, "dksjdskjdk"), true)
        val item2 = AppChooserView(AppChooser("dddd", bitmap3, "dksjdvkjdk"), true)
        val item3 = AppChooserView(AppChooser("dhjsdhd", bitmap4, "dksjbdkjdk"), true)
        val item4 = AppChooserView(AppChooser("dhjhdjs", bitmap5, "dksjddkjdk"), true)
        val item5 = AppChooserView(AppChooser("dgdy", bitmap6, "dksjwdkjdk"), true)
        val item6 = AppChooserView(AppChooser("hhhh", bitmap7, "dksjdqkjdk"), true)
        val item7 = AppChooserView(AppChooser("kkkk", bitmap8, "dksjdekjdk"), true)
        val item8 = AppChooserView(AppChooser("gggg", bitmap9, "dksj3dkjdk"), true)

        val listTmp = mutableListOf<AppChooserView>()
        listTmp.add(item)
        listTmp.add(item1)
        listTmp.add(item2)
        listTmp.add(item3)
        listTmp.add(item4)
        listTmp.add(item5)
        listTmp.add(item6)
        listTmp.add(item7)
        listTmp.add(item8)
        _listApp.postValue(listTmp)
        return listApp
    }

}
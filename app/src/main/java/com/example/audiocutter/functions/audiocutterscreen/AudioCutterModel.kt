package com.example.audiocutter.functions.audiocutterscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.objects.AudioFile

class AudioCutterModel : ViewModel() {
    private val TAG = AudioCutterModel::class.java.name

    private var _listAllFileByType: MutableLiveData<List<AudioFile>> = MutableLiveData()

    val listAllFileByType: LiveData<List<AudioFile>>
        get() = _listAllFileByType


    suspend fun getListAllFileByType(context: Context): LiveData<List<AudioFile>> {

        var listData = AudioFileManagerImpl().getAllListByType(context).value!!
        _listAllFileByType.postValue(listData)
        Log.d(TAG, "getListAllFileByType: ${listData.size}")

        return listAllFileByType
    }
}
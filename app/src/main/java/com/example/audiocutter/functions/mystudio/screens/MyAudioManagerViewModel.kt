package com.example.audiocutter.functions.mystudio.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.objects.ActionData

class MyAudioManagerViewModel : BaseViewModel() {

    private val actionLiveData: MutableLiveData<ActionData> = MutableLiveData()

    fun getAction(): LiveData<ActionData> {
        return actionLiveData
    }

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        super.onReceivedAction(fragmentMeta)

        fragmentMeta.data?.let {
            val data = fragmentMeta.data as Int
            when (fragmentMeta.action) {
                Constance.ACTION_DELETE -> {
                    actionLiveData.postValue(ActionData(Constance.ACTION_DELETE, data))
                }
            }
        }
    }
}
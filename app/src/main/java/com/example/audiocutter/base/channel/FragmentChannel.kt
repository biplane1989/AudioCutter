package com.example.audiocutter.base.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audiocutter.base.BaseFragment

object FragmentChannel {
    private val fragmentMetaLiveData = MutableLiveData<FragmentMeta>()

    fun sendAction(fragmentMeta: FragmentMeta) {
        fragmentMetaLiveData.postValue(fragmentMeta)
    }

    fun getFragmentMeta(): LiveData<FragmentMeta> {
        return fragmentMetaLiveData
    }

}
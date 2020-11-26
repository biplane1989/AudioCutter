package com.example.audiocutter.functions.flashcall.sreens

import androidx.lifecycle.LiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.manager.FlashCallConfig
import com.example.audiocutter.core.manager.ManagerFactory

class FlashCallModel : BaseViewModel() {

    fun getFlashCallConfig(): LiveData<FlashCallConfig> {
        return ManagerFactory.getFlashCallSetting().getFlashCallConfig()
    }

}
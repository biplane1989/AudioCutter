package com.example.audiocutter.functions.mergescreen.event

import com.example.audiocutter.functions.audiocutterscreen.objs.AudioCutterView

interface OnActionCallback {
    fun sendAndReceiveData(listData: List<AudioCutterView>)
    fun backFrg()

}
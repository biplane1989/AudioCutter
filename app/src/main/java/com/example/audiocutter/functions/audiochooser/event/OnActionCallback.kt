package com.example.audiocutter.functions.audiochooser.event

import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView

interface OnActionCallback {
    fun sendAndReceiveData(listData: List<AudioCutterView>)
    fun backFrg()

}
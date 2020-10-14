package com.example.audiocutter.functions.audiochooser.merge.event

import com.example.audiocutter.functions.audiochooser.cut.objs.AudioCutterView

interface OnActionCallback {
    fun sendAndReceiveData(listData: List<AudioCutterView>) {

    }

    fun backFrg() {

    }
    fun showEmptyCallback(){

    }

    fun hideProgress() {

    }

}
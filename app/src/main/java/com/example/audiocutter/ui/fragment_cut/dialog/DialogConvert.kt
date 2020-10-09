package com.example.audiocutter.ui.fragment_cut.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils

class DialogConvert : BaseDialog() {

    private lateinit var audioFile: AudioFile

    override fun getLayoutResId(): Int {
        return R.layout.dialog_convert
    }

    companion object {
        fun showDialogConvert(fragmentManager: FragmentManager, audioFile: AudioFile) {
            val bundle = Bundle()
            bundle.putSerializable(Utils.KEY_SEND_AUDIO, audioFile)
            val dialogConvert = DialogConvert()
            dialogConvert.arguments = bundle
            dialogConvert.audioFile = audioFile
            dialogConvert.show(fragmentManager, "dialog convert")
        }
    }
}
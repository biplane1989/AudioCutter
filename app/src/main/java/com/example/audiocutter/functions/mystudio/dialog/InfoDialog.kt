package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_dialog_info.*
import kotlinx.android.synthetic.main.output_audio_manager_screen_dialog_delete.*
import java.text.SimpleDateFormat

class InfoDialog : BaseDialog() {

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        lateinit var audioFile: AudioFile

        @JvmStatic
        fun newInstance(audioFile: AudioFile): InfoDialog {
            val dialog = InfoDialog()
            val bundle = Bundle()
            bundle.putSerializable(BUNDLE_NAME_KEY, audioFile)
            dialog.arguments = bundle

            this.audioFile = audioFile
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getData()
        tv_ok.setOnClickListener(View.OnClickListener {

            dialog?.dismiss()
        })
    }

    fun getData() {
        val simpleDateFormat = SimpleDateFormat("mm:ss")

        tv_file.text = audioFile.fileName
        tv_location.text = audioFile.file.absolutePath
        tv_size.text = audioFile.size.toString() + "kb"
        tv_length.text = simpleDateFormat.format(audioFile.time)
        tv_birate.text = audioFile.bitRate.toString() + "kb/s"
        tv_title.text = "Unknown"
        tv_artist.text = "Unknown"
        tv_album.text = "Unknown"
        tv_genre.text = "Unknown"
    }
}
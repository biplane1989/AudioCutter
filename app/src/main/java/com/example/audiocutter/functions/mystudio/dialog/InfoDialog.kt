package com.example.audiocutter.functions.mystudio.dialog

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import com.example.core.utils.FileUtil
import kotlinx.android.synthetic.main.my_studio_dialog_info.*
import java.io.File
import java.text.SimpleDateFormat

class InfoDialog : BaseDialog() {

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_FILE_NAME = "BUNDLE_FILE_NAME"
        val BUNDLE_FILE_PATH = "BUNDLE_FILE_PATH"
        lateinit var audioFile: AudioFile
        val UNKNOWN = "Unknown"

        @JvmStatic
        fun newInstance(fileName: String, filePath: String): InfoDialog {
            val dialog = InfoDialog()
            val bundle = Bundle()
            bundle.putString(BUNDLE_FILE_NAME, fileName)
            bundle.putString(BUNDLE_FILE_PATH, filePath)
            dialog.arguments = bundle
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

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun getData() {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")

        if (requireArguments().getString(BUNDLE_FILE_PATH) != null) {
            val size = File(requireArguments().getString(BUNDLE_FILE_PATH).toString()).length()
            val index = requireArguments().getString(BUNDLE_FILE_PATH)?.lastIndexOf(".")
            val format = requireArguments().getString(BUNDLE_FILE_PATH)
                ?.substring(index!! + 1, requireArguments().getString(BUNDLE_FILE_PATH)?.length!!)

            tv_format.text = "$format($format)"
            tv_file.text = requireArguments().getString(BUNDLE_FILE_NAME)
            tv_size.text = (size.toInt() / 1024).toString() + " kb" + " (" + size + " bytes)"
            tv_location.text = requireArguments().getString(BUNDLE_FILE_PATH).toString()


            ManagerFactory.getAudioFileManager()
                .findAudioFile(requireArguments().getString(BUNDLE_FILE_PATH).toString())
                ?.let { audioFile ->
                    if (audioFile.artist != null) {
                        tv_artist.text = audioFile.artist
                    } else {
                        tv_artist.text = UNKNOWN
                    }

                    if (audioFile.alBum != null) {
                        tv_album.text = audioFile.alBum
                    } else {
                        tv_album.text = UNKNOWN
                    }

                    tv_birate.text = (audioFile.bitRate / 1000).toString() + " kb/s"

                    if (audioFile.title != null) {
                        tv_title.text = audioFile.title
                    } else {
                        tv_title.text = UNKNOWN
                    }

                    tv_length.text = simpleDateFormat.format(audioFile.duration.toInt())

                    if (audioFile.genre != null) {
                        tv_genre.text = audioFile.genre
                    } else {
                        tv_genre.text = UNKNOWN
                    }

                    tv_date.text = audioFile.modifiedStr
                }
        } else {
            tv_file.text = UNKNOWN
            tv_size.text = UNKNOWN
            tv_location.text = UNKNOWN
            tv_format.text = UNKNOWN
        }
    }
}
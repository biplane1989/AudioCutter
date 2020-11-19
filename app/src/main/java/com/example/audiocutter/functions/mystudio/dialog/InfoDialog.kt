package com.example.audiocutter.functions.mystudio.dialog

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
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

    fun getData() {
        val simpleDateFormat = SimpleDateFormat("mm:ss")

        /*if (requireArguments().getString(BUNDLE_FILE_PATH) != null) {
            val size = File(requireArguments().getString(BUNDLE_FILE_PATH).toString()).length()
            val index = requireArguments().getString(BUNDLE_FILE_PATH)?.lastIndexOf(".")
            val format = requireArguments().getString(BUNDLE_FILE_PATH)
                ?.substring(index!! + 1, requireArguments().getString(BUNDLE_FILE_PATH)?.length!!)

            tv_format.text = format + "(" + format + ")"
            tv_file.text = requireArguments().getString(BUNDLE_FILE_NAME)
            tv_size.text = (size.toInt() / 1024).toString() + " kb" + " (" + size + " bytes)"
            tv_location.text = requireArguments().getString(BUNDLE_FILE_PATH).toString()
        } else {
            tv_file.text = UNKNOWN
            tv_size.text = UNKNOWN
            tv_location.text = UNKNOWN
            tv_format.text = UNKNOWN
        }

        if (!artist.isNullOrEmpty()) {
            tv_artist.text = artist
        } else {
            tv_artist.text = UNKNOWN
        }

        if (!album.isNullOrEmpty()) {
            tv_album.text = album
        } else {
            tv_album.text = UNKNOWN
        }
        if (!bitRate.isNullOrEmpty()) {
            tv_birate.text = (bitRate.toInt() / 10000).toString() + " kb/s"
        } else {
            tv_birate.text = UNKNOWN
        }

        if (!title.isNullOrEmpty()) {
            tv_title.text = title
        } else {
            tv_title.text = UNKNOWN
        }

        if (!duration.isNullOrEmpty()) {
            tv_length.text = simpleDateFormat.format(duration.toInt())
        } else {
            tv_length.text = UNKNOWN
        }

        if (!genre.isNullOrEmpty()) {
            tv_genre.text = genre
        } else {
            tv_genre.text = UNKNOWN
        }

        if (!date.isNullOrEmpty()) {
            tv_date.text = date
        } else {
            tv_date.text = UNKNOWN
        }*/

    }
}
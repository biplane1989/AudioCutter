package com.example.audiocutter.functions.audiochooser.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.merge_audio_dialog.*

class MergeDialog(mContext: Context) : Dialog(mContext), View.OnClickListener {
    private var countFile = 0
    private lateinit var mCallback: MergeDialogListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.merge_audio_dialog)
        initViews()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun setOnCallBack(event: MergeDialogListener) {
        mCallback = event
    }

    private fun initViews() {
        edt_filename_dialog.setText(Utils.genNewAudioFileName(Folder.TYPE_MERGER))
        tv_cancel_dialog_merge.setOnClickListener(this)
        tv_ok_dialog_merge.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun show() {
        super.show()
        tv_count_file_dialog.text = "$countFile ${context.getString(R.string.file_merged)}"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_cancel_dialog_merge -> {
                mCallback.cancalKeybroad()
                dismiss()
            }
            R.id.tv_ok_dialog_merge -> {
                val name = Utils
                    .createValidFileName(
                        edt_filename_dialog.text.toString().trim(),
                        Folder.TYPE_MERGER
                    )
                if (checkValid(edt_filename_dialog.text.toString())) {
                    mCallback.mergeAudioFile(name)
                    dismiss()
                } else {
                    edt_filename_dialog.setText(name)
                }


            }
        }
    }

    private fun checkValid(name: String): Boolean {
        if (name.isEmpty()) {
            edt_filename_dialog.error = "Name must be null"
            edt_filename_dialog.requestFocus()
            return false
        }
        return true

    }

    fun sendData(size: Int) {
        countFile = size
    }

    interface MergeDialogListener {
        fun mergeAudioFile(filename: String)
        fun cancalKeybroad()
    }


}
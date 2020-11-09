package com.example.audiocutter.functions.editor.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.functions.mystudio.Constance
import kotlinx.android.synthetic.main.mix_dialog_file_name.*

class FileNameDialog : BaseDialog() {


    companion object {
        val TAG = "fileNameDialog"
        val MIX_FILE_NAME_KEY = "MIX_FILE_NAME_KEY"
        lateinit var dialogListener: FileNameDialogListener

        @JvmStatic
        fun newInstance(listener: FileNameDialogListener): FileNameDialog {
            this.dialogListener = listener
            val dialog = FileNameDialog()
            val bundle = Bundle()
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.mix_dialog_file_name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_cancel_dialog_filename.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
            dialogListener.onCancel()
        })

        tv_mix_dialog_filename.setOnClickListener(View.OnClickListener {
            dialogListener.onMixClick(edt_file_name.text.toString())
            if (!edt_file_name.text.toString().isNullOrBlank()) {
                dismiss()
            }
        })
    }
}

interface FileNameDialogListener {
    fun onMixClick(fileName: String)
    fun onCancel()
}
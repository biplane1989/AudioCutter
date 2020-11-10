package com.example.audiocutter.functions.editor.dialogs

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.android.synthetic.main.mix_dialog_file_name.*

class MixerDialog : BaseDialog() {


    companion object {
        val TAG = "fileNameDialog"
        val MIX_FILE_NAME_KEY = "MIX_FILE_NAME_KEY"
        lateinit var dialogListener: FileNameDialogListener

        @JvmStatic
        fun newInstance(listener: FileNameDialogListener): MixerDialog {
            this.dialogListener = listener
            val dialog = MixerDialog()
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
        edt_file_name.setText(ManagerFactory.getAudioFileManager().genNewAudioFileName(Folder.TYPE_MIXER))
        val name = ManagerFactory.getAudioFileManager()
            .createValidFileName(
                edt_file_name.text.toString().trim(),
                Folder.TYPE_MIXER
            )

        tv_cancel_dialog_filename.setOnClickListener {
            dialog?.dismiss()
            dialogListener.onCancel()
        }

        tv_mix_dialog_filename.run {

            tv_cancel_dialog_filename.setOnClickListener {
                dialog?.dismiss()
                dialogListener.onCancel()
            }

            tv_mix_dialog_filename.setOnClickListener {
                if (checkValid(edt_file_name.text.toString())) {
                    dialogListener.onMixClick(edt_file_name.text.toString())
                    dismiss()
                } else {
                    edt_file_name.setText(name)
                }
            }
        }
    }

    private fun checkValid(name: String): Boolean {
        if (name.isEmpty()) {
            edt_file_name.error = "Name must be null"
            edt_file_name.requestFocus()
            return false
        }
        return true
    }
}

interface FileNameDialogListener {
    fun onMixClick(fileName: String)
    fun onCancel()
}
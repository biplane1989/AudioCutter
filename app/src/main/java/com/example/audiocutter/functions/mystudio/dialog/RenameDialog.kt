package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import kotlinx.android.synthetic.main.my_studio_dialog_rename.*

class RenameDialog : BaseDialog() {

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        val BUNDLE_TYPE_KEY = "BUNDLE_TYPE_KEY"
        val BUNDLE_PATH_KEY = "BUNDLE_PATH_KEY"
        lateinit var dialogListener: RenameDialogListener

        @JvmStatic
        fun newInstance(
            listener: RenameDialogListener,
            type: Int,
            filePath: String,
            mimeType: String
        ): RenameDialog {
            this.dialogListener = listener
            val dialog = RenameDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_TYPE_KEY, type)
            bundle.putString(BUNDLE_PATH_KEY, filePath)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)

    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_rename
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_cancel_dialog_delete.setOnClickListener {
            dialog?.dismiss()
        }

        tv_rename_dialog_delete.setOnClickListener {

            if (checkValid(edt_new_name.text.toString())) {
                dialogListener.onRenameClick(
                    edt_new_name.text.toString(),
                    requireArguments().getInt(BUNDLE_TYPE_KEY),
                    requireArguments().getString(BUNDLE_PATH_KEY, "")
                )
                dismiss()
            }
        }
    }

    private fun checkValid(name: String): Boolean {
        val typeFolder: Folder = when (requireArguments().getInt(BUNDLE_TYPE_KEY)) {
            0 ->
                Folder.TYPE_CUTTER

            1 ->
                Folder.TYPE_MERGER

            else ->
                Folder.TYPE_MIXER
        }
        if (name.isEmpty()) {
            edt_new_name.error = "Name must be null"
            edt_new_name.requestFocus()
            return false
        }
        if (ManagerFactory.getAudioFileManager()
                .checkFileNameDuplicate(edt_new_name.text.toString(), typeFolder)
        ) {
            edt_new_name.error = "The file name already exists"
            edt_new_name.requestFocus()
        val a=    ManagerFactory.getAudioFileManager().buildAudioFile("path")
            return false
        }
        return true
    }
}

interface RenameDialogListener {
    fun onRenameClick(
        name: String, type: Int,
        filePath: String
    )
}
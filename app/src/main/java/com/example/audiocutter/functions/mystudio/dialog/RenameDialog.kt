package com.example.audiocutter.functions.mystudio.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
            name: String
        ): RenameDialog {
            this.dialogListener = listener
            val dialog = RenameDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_TYPE_KEY, type)
            bundle.putString(BUNDLE_PATH_KEY, filePath)
            bundle.putString(BUNDLE_NAME_KEY, name)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
       /* dialog?.let {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = ((requireContext().resources?.displayMetrics?.widthPixels)!! * 0.90).toInt()
            dialog!!.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }*/


    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_rename
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edt_new_name.setText(requireArguments().getString(BUNDLE_NAME_KEY, ""))
        tv_cancel_dialog.setOnClickListener {
            dialog?.dismiss()
        }

        tv_rename_dialog.setOnClickListener {

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
            edt_new_name.error = context?.resources?.getString(R.string.name_mustbenull)
            edt_new_name.requestFocus()
            return false
        }
        if (ManagerFactory.getAudioFileManager()
                .checkFileNameDuplicate(edt_new_name.text.toString(), typeFolder)
        ) {
            edt_new_name.error = context?.resources?.getString(R.string.filename_exist)
            edt_new_name.requestFocus()
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
package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import kotlinx.android.synthetic.main.my_studio_dialog_rename.*

class RenameDialog : BaseDialog() {

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        lateinit var dialogListener: RenameDialogListener

        @JvmStatic
        fun newInstance(listener: RenameDialogListener, name: String): RenameDialog {
            this.dialogListener = listener
            val dialog = RenameDialog()
            val bundle = Bundle()
            bundle.putString(BUNDLE_NAME_KEY, name)
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

        tv_cancel_dialog_delete.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
        })

        tv_delete_dialog_delete.setOnClickListener(View.OnClickListener {
            dialogListener.onRenameClick()
        })
    }
}

interface RenameDialogListener {
    fun onRenameClick()
}
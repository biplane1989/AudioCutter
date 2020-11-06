package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import kotlinx.android.synthetic.main.my_studio_dialog_delete.*

class DeleteDialog : BaseDialog() {

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        lateinit var dialogListener: DeleteDialogListener

        @JvmStatic
        fun newInstance(listener: DeleteDialogListener): DeleteDialog {
            this.dialogListener = listener
            val dialog = DeleteDialog()
            val bundle = Bundle()
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_delete
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_cancel_dialog_delete.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
            dialogListener.onCancel()
        })

        tv_delete_dialog_delete.setOnClickListener(View.OnClickListener {
            dialogListener.onDeleteClick()
            dialog?.dismiss()
        })
    }
}

interface DeleteDialogListener {
    fun onDeleteClick()
    fun onCancel()
}
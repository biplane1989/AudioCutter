package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import kotlinx.android.synthetic.main.my_studio_dialog_delete.*

class CancelDialog : BaseDialog() {


    companion object {
        val TAG = "CancelDialog"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        lateinit var dialogListener: CancelDialogListener

        @JvmStatic
        fun newInstance(listener: CancelDialogListener, id: Int): CancelDialog {
            this.dialogListener = listener
            val dialog = CancelDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, id)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_cancel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_cancel_dialog_delete.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
            dialogListener.onCancelDialog()
        })

        tv_delete_dialog_delete.setOnClickListener(View.OnClickListener {
            dialogListener.onCancelDeleteClick(requireArguments().getInt(BUNDLE_NAME_KEY))
            dialog?.dismiss()
        })
    }
}

interface CancelDialogListener {
    fun onCancelDeleteClick(id: Int)
    fun onCancelDialog()
}
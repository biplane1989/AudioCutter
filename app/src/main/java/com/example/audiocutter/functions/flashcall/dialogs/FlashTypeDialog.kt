package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

class FlashTypeDialog : BaseDialog(), View.OnClickListener {
    private lateinit var tvCancel: TextView
    private lateinit var tvOk: TextView


    override fun initViews(view: View, savedInstanceState: Bundle?) {
        tvCancel = view.findViewById(R.id.tv_cancel_dialog_flash_type)
        tvOk = view.findViewById(R.id.tv_ok_dialog_flash_type)
        tvCancel.setOnClickListener(this)
        tvOk.setOnClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun getLayoutResId(): Int {
        return R.layout.flashcall_screens_dialog_flashing_type
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_ok_dialog_flash_type -> {
                dismiss()
            }
            R.id.tv_cancel_dialog_flash_type -> {
                dismiss()
            }
        }
    }
}
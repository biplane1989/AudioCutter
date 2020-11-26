package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

enum class TypeFlash() {
    CONTINUITY, BEAT
}

class FlashTypeDialog : BaseDialog(), View.OnClickListener {
    private lateinit var tvCancel: TextView
    private lateinit var tvOk: TextView
    private lateinit var rbContinuity: RadioButton
    private lateinit var rbBeat: RadioButton
    private var type: TypeFlash? = null
    private lateinit var mCallBack: FlashTypeListener


    fun setOnCallBack(event: FlashTypeListener) {
        mCallBack = event
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        tvCancel = view.findViewById(R.id.tv_cancel_dialog_flash_type)
        tvOk = view.findViewById(R.id.tv_ok_dialog_flash_type)
        rbBeat = view.findViewById(R.id.rb_beat)
        rbContinuity = view.findViewById(R.id.rb_continuity)
        tvCancel.setOnClickListener(this)
        tvOk.setOnClickListener(this)
        rbContinuity.setOnClickListener(this)
        rbBeat.setOnClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun getLayoutResId(): Int {
        return R.layout.flashcall_screens_dialog_flashing_type
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rb_continuity -> {
                type = TypeFlash.CONTINUITY
            }
            R.id.rb_beat -> {
                type = TypeFlash.BEAT
            }
            R.id.tv_ok_dialog_flash_type -> {
                if (type != null) {
                    mCallBack.changeMode(type!!)
                }
            }
            R.id.tv_cancel_dialog_flash_type -> {
                dismiss()
            }
        }
    }

    interface FlashTypeListener {
        fun changeMode(type: TypeFlash)
    }
}
package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.manager.FlashType


class FlashTypeDialog : BaseDialog(), View.OnClickListener {
    private lateinit var tvCancel: TextView
    private lateinit var tvOk: TextView
    private lateinit var rbContinuity: RadioButton
    private lateinit var rbBeat: RadioButton
    private var type: FlashType? = null
    private lateinit var mCallBack: FlashTypeListener


    fun setOnCallBack(event: FlashTypeListener) {
        mCallBack = event
    }

    companion object {
        val KEY_TYPE = "KEY_TYPE"

        @JvmStatic
        fun newInstance(num: Int): FlashTypeDialog {
            val args = Bundle()
            val dialog = FlashTypeDialog()
            args.putInt(KEY_TYPE, num)
            dialog.arguments = args
            return dialog
        }
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

        when (requireArguments().getInt(KEY_TYPE)) {
            1 -> {
                type = FlashType.CONTINUITY
                rbContinuity.isChecked = true
            }
            0 -> {
                type = FlashType.BEAT
                rbBeat.isChecked = true
            }
        }
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
                type = FlashType.CONTINUITY
            }
            R.id.rb_beat -> {
                type = FlashType.BEAT
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
        fun changeMode(type: FlashType)
    }
}
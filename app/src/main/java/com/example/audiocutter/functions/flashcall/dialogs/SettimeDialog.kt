package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

class SettimeDialog : BaseDialog(), View.OnClickListener {
    private lateinit var tvCancel: TextView
    private lateinit var tvOk: TextView
    private lateinit var tpTime: TimePicker
    private lateinit var mCallback: SettimeListener


    fun setOnCallBack(event: SettimeListener) {
        mCallback = event
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        tvCancel = view.findViewById(R.id.tv_cancel_dialog_settime)
        tvOk = view.findViewById(R.id.tv_ok_dialog_settime)

        tpTime = view.findViewById(R.id.tp_time_flash)
        tvCancel.setOnClickListener(this)
        tvOk.setOnClickListener(this)


        tpTime.setOnTimeChangedListener { _, hour, minute ->
            Log.d("TAG", "initViews: hours $hour  minute $minute")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun getLayoutResId(): Int {
        return R.layout.flashcall_screen_dialog_set_time
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_ok_dialog_settime -> {
                mCallback.prevFrg()
            }
            R.id.tv_cancel_dialog_settime -> {
                dismiss()
            }
        }
    }

    interface SettimeListener {
        fun prevFrg()
    }

}
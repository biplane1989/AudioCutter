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
    private var hoursChooser = 0
    private var minuteChooser = 0


    fun setOnCallBack(event: SettimeListener) {
        mCallback = event
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        tvCancel = view.findViewById(R.id.tv_cancel_dialog_settime)
        tvOk = view.findViewById(R.id.tv_ok_dialog_settime)

        tpTime = view.findViewById(R.id.tp_time_flash)
        tvCancel.setOnClickListener(this)
        tvOk.setOnClickListener(this)
        hoursChooser = tpTime.currentHour
        minuteChooser = tpTime.currentMinute


        tpTime.setOnTimeChangedListener { _, hour, minute ->
            Log.d("TAG", "initViews: hours $hour  minute $minute")
            hoursChooser = hour
            minuteChooser = minute
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun getLayoutResId(): Int {
        return R.layout.flashcall_screen_dialog_set_time
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_ok_dialog_settime -> {
                mCallback.changeTimeFlash(hoursChooser, minuteChooser)
            }
            R.id.tv_cancel_dialog_settime -> {
                dismiss()
            }
        }
    }

    interface SettimeListener {
        fun changeTimeFlash(hours: Int, minute: Int)
    }

}
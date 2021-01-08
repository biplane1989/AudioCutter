package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.functions.mystudio.dialog.RenameDialog
import com.example.audiocutter.functions.mystudio.dialog.RenameDialogListener
import com.google.android.material.snackbar.Snackbar

class SettimeDialog : BaseDialog(), View.OnClickListener {
    private lateinit var tvCancel: TextView
    private lateinit var tvOk: TextView
    private lateinit var tpTime: TimePicker
    private var hoursChooser = 0
    private var minuteChooser = 0
//    private lateinit var mCallback: SettimeListener

    companion object {
        val TAG = "SetTimeDialog"
        val BUNDLE_HOUR_KEY = "BUNDLE_HOUR_KEY"
        val BUNDLE_MINUTE_KEY = "BUNDLE_MINUTE_KEY"
        val BUNDLE_TYPE_KEY = "BUNDLE_TYPE_KEY"
        lateinit var mCallback: SettimeListener

        @JvmStatic
        fun newInstance(event: SettimeListener, hours: Int, minute: Int, typeSetTime: Int): SettimeDialog {
            this.mCallback = event
            val dialog = SettimeDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_HOUR_KEY, hours)
            bundle.putInt(BUNDLE_MINUTE_KEY, minute)
            bundle.putInt(BUNDLE_TYPE_KEY, typeSetTime)
            dialog.arguments = bundle
            return dialog
        }
    }


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
                if (requireArguments().getInt(SettimeDialog.BUNDLE_TYPE_KEY, 0) == 2) {
                    if (hoursChooser >= requireArguments().getInt(SettimeDialog.BUNDLE_HOUR_KEY, 0) && minuteChooser >= requireArguments().getInt(SettimeDialog.BUNDLE_MINUTE_KEY, 0)) {
                        mCallback.changeTimeFlash(hoursChooser, minuteChooser)
                    } else {
                        view?.let {
                            val dialogSnack = Snackbar.make(requireView(), getString(R.string.set_time_dialog_notification_set_end_time_error), Snackbar.LENGTH_SHORT)
                            dialogSnack.show()
                        }
                    }
                } else {
                    mCallback.changeTimeFlash(hoursChooser, minuteChooser)
                }
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
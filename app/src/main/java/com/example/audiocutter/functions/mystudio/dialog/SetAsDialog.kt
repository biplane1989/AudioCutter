package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.functions.mystudio.Constance
import kotlinx.android.synthetic.main.output_audio_manager_screen_dialog_set_as.*

class SetAsDialog : BaseDialog() {

    var typeSet: Int = -1

    companion object {
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        val TAG = "SetAsDialog"
        lateinit var listener: SetAsDialogListener

        @JvmStatic
        fun newInstance(setAsDialogListener: SetAsDialogListener, name: String): SetAsDialog {
            listener = setAsDialogListener
            val dialog = SetAsDialog()
            val bundle = Bundle()
            bundle.putString(BUNDLE_NAME_KEY, name)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.output_audio_manager_screen_dialog_set_as
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_cancel_dialog_set_as.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        rb_alarm.setOnClickListener(View.OnClickListener {
            typeSet = Constance.ALARM_TYPE
        })
        rb_notification.setOnClickListener(View.OnClickListener {
            typeSet = Constance.NOTIFICATION_TYPE
        })
        rb_ringtone.setOnClickListener(View.OnClickListener {
            typeSet = Constance.NOTIFICATION_TYPE
        })

        tv_set_dialog_set_as.setOnClickListener(View.OnClickListener {
            when (typeSet) {
                Constance.RINGTONE_TYPE -> {
                    listener.onsetAsListenner(Constance.RINGTONE_TYPE)
                }
                Constance.ALARM_TYPE -> {
                    listener.onsetAsListenner(Constance.ALARM_TYPE)
                }
                Constance.NOTIFICATION_TYPE -> {
                    listener.onsetAsListenner(Constance.NOTIFICATION_TYPE)
                }
            }
            dismiss()
        })
    }

}

interface SetAsDialogListener {
    fun onsetAsListenner(type: Int)
}
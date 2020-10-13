package com.example.audiocutter.functions.audiochooser.cut.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiochooser.cut.objs.TypeAudioSetAs
import kotlinx.android.synthetic.main.setas_dialog.*

class SetAsDialog(context: Context) : Dialog(context), View.OnClickListener {
    var typeSet: TypeAudioSetAs? = null


    lateinit var mCallback: setAsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setas_dialog)
        initViews()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun setOnCallBack(event: setAsListener) {
        mCallback = event
    }

    private fun initViews() {

        tv_cancel_dialog_set_as.setOnClickListener(this)
        tv_set_dialog_set_as.setOnClickListener(this)

        rb_alarm.setOnClickListener(this)
        rb_ringtone.setOnClickListener(this)
        rb_notification.setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.rb_alarm -> typeSet = TypeAudioSetAs.ALARM

            R.id.rb_notification -> typeSet = TypeAudioSetAs.NOTIFICATION

            R.id.rb_ringtone -> typeSet = TypeAudioSetAs.RINGTONE

            R.id.tv_cancel_dialog_set_as -> dismiss()

            R.id.tv_set_dialog_set_as -> setAsAudioFile()
        }
    }

    private fun setAsAudioFile() {
        if (typeSet == null) {
            Toast.makeText(context, "please enter choose", Toast.LENGTH_SHORT).show()
        } else {
            mCallback.setAudioAs(typeSet!!)
        }
    }

    interface setAsListener {
        fun setAudioAs(typeAudioSetAs: TypeAudioSetAs)
    }

}


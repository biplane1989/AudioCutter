package com.example.audiocutter.functions.flashcall.dialogs

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

class NotificationDialog : BaseDialog() {
    private lateinit var ivCancel: ImageView
    private lateinit var btAllow: Button
    private lateinit var mCallback: NotifiCationListener


    fun setOnCallBack(event: NotifiCationListener) {
        mCallback = event
    }


    override fun getLayoutResId(): Int {
        return R.layout.notification_permission_dialog_flashcall
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        ivCancel = view.findViewById(R.id.iv_close_button_notification)
        btAllow = view.findViewById(R.id.allow_button_notification)
        ivCancel.setOnClickListener {
            dismiss()
        }

        btAllow.setOnClickListener {
            mCallback.allowNotificationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    interface NotifiCationListener {
        fun allowNotificationPermission()
    }
}
package com.example.audiocutter.functions.common

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

class ContactPermissionDialog(val onAcceptListener: () -> Unit) : BaseDialog(),
    View.OnClickListener {

    override fun getLayoutResId(): Int {
        return R.layout.common_contact_permission_dialog
    }

    private lateinit var closeButton: View
    private lateinit var allowButton: View

    companion object {
        fun newInstance(onAcceptListener: () -> Unit): ContactPermissionDialog {
            return ContactPermissionDialog(onAcceptListener)
        }
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        super.initViews(view, savedInstanceState)
        closeButton = view.findViewById(R.id.close_button)
        allowButton = view.findViewById(R.id.allow_button)
        closeButton.setOnClickListener(this)
        allowButton.setOnClickListener(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun onClick(view: View) {
        when (view) {
            closeButton -> {
                dismiss()
            }
            allowButton -> {
                dismiss()
                onAcceptListener()
            }
        }
    }

}
package com.example.audiocutter.functions.audiochooser.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import kotlinx.android.synthetic.main.cutchooser_set_as_done_dialog.*

class SetAsDoneDialog(context: Context) : Dialog(context), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cutchooser_set_as_done_dialog)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initViews()
    }

    private fun initViews() {
        iv_cancel_dialog_done.setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        if (p0.id == R.id.iv_cancel_dialog_done) {
            dismiss()
        }
    }

}
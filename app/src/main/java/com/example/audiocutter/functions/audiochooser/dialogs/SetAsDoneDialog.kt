package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import kotlinx.android.synthetic.main.cutchooser_set_as_done_dialog.*

class SetAsDoneDialog(val mContext: Context) : BaseDialog(), View.OnClickListener {
    private lateinit var ivCancel :ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun getLayoutResId(): Int {
        return R.layout.cutchooser_set_as_done_dialog
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        ivCancel= view.findViewById(R.id.iv_cancel_dialog_done)
        ivCancel.setOnClickListener(this)
    }


    override fun onClick(p0: View) {
        if (p0.id == R.id.iv_cancel_dialog_done) {
            dismiss()
        }
    }

}
package com.example.audiocutter.functions.flashcall.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog

class SuggestionDialog : BaseDialog() {

    private lateinit var ivCancel: ImageView
    private lateinit var ctDialog: ConstraintLayout

    override fun getLayoutResId(): Int {
        return R.layout.suggestions_dialog
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        super.initViews(view, savedInstanceState)

        ctDialog = view.findViewById(R.id.ct_dialog)
        ctDialog.setOnClickListener {
            dismiss()
        }
        ivCancel = view.findViewById(R.id.iv_cancel_dialog)
        ivCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }
}
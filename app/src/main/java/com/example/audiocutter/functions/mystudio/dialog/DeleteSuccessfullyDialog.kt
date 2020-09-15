package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import kotlinx.android.synthetic.main.output_audio_manager_screen_dialog_delete_successfully.*

class DeleteSuccessfullyDialog : BaseDialog() {

    override fun getLayoutResId(): Int {
        return R.layout.output_audio_manager_screen_dialog_delete_successfully
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_close.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
        })
    }
}
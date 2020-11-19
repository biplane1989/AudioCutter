package com.example.audiocutter.functions.settings.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.audiocutter.R

class SetLanguageDialog(val mContext: Context) : DialogFragment(), View.OnClickListener {
    private lateinit var rootView: View
    private lateinit var rbVietNam: RadioButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var tvCancel: TextView
    private lateinit var tvOK: TextView
    private lateinit var mCallBack: DialogSettingsListener
    private var indexItem = 0


    fun setOnCallBack(event: DialogSettingsListener) {
        mCallBack = event
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.settingscrren_language_dialog, container, false)
        initViews()
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)

    }


    fun initViews() {

        rbVietNam = rootView.findViewById(R.id.rb_vietnam)
        rbEnglish = rootView.findViewById(R.id.rb_english)
        tvCancel = rootView.findViewById(R.id.tv_cancel_dialog_setting_language)
        tvOK = rootView.findViewById(R.id.tv_ok_dialog_setting_language)

        rbEnglish.setOnClickListener(this)
        rbVietNam.setOnClickListener(this)
        tvOK.setOnClickListener(this)
        tvCancel.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v) {
            rbEnglish -> {
                indexItem = 1
            }
            rbVietNam -> {
                indexItem = 0
            }
            tvOK -> {
                mCallBack.setLanguage(indexItem)
                dismiss()
            }
            tvCancel -> {
                dismiss()
            }
        }
    }

    interface DialogSettingsListener {
        fun setLanguage(item: Int)
    }

}
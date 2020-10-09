package com.example.audiocutter.ui.fragment_cut.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.databinding.DialogAdvancedBinding
import com.example.audiocutter.util.PreferencesHelper
import com.example.core.core.Effect

class DialogAdvanced : Dialog, View.OnClickListener {
    private lateinit var advancedBinding: DialogAdvancedBinding
    private lateinit var listener: OnDialogAdvanceListener
    private var fadeInPos = 0
    private var fadeOutPos = 0
    private val valuesEffect = Effect.values()

    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        initView(context)
        initData()
    }

    private fun initData() {
        fadeInPos = PreferencesHelper.getInt(PreferencesHelper.FADE_IN_TIME, 0)
        fadeOutPos = PreferencesHelper.getInt(PreferencesHelper.FADE_OUT_TIME, 0)

        val list = ArrayList<String>()
        for (i in valuesEffect.indices) {
            if (i == 0) {
                list.add(valuesEffect[i].name.upperFirstString())
            } else {
                list.add(
                    if (valuesEffect[i].time == 3) valuesEffect[i].time.toString()
                        .plus("s (Recommend)") else valuesEffect[i].time.toString().plus("s")
                )
            }
        }

        advancedBinding.spinnerFadeIn.apply {
            setItems(list)
            selectedIndex = fadeInPos
            setOnItemSelectedListener { view, position, id, item ->
                fadeInPos = position
            }
        }
        advancedBinding.spinnerFadeOut.apply {
            setItems(list)
            selectedIndex = fadeOutPos
            setOnItemSelectedListener { view, position, id, item ->
                fadeOutPos = position
            }
        }
    }

    private fun String.upperFirstString(): String {
        val string = this.toLowerCase()
        var string1 = string.substring(0, 1).toUpperCase()
        return string1.plus(string.substring(1, string.length))
    }

    private fun initView(context: Context) {
//        if (window != null) window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        advancedBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_advanced,
            null,
            false
        )
        setContentView(advancedBinding.root)
        advancedBinding.cancelTv.setOnClickListener(this)
        advancedBinding.okTv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            advancedBinding.cancelTv -> {
                dismiss()
            }
            advancedBinding.okTv -> {
                PreferencesHelper.putInt(
                    PreferencesHelper.FADE_IN_TIME, fadeInPos
                )
                PreferencesHelper.putInt(
                    PreferencesHelper.FADE_OUT_TIME, fadeOutPos
                )
                listener.onDialogOk(valuesEffect[fadeInPos], valuesEffect[fadeOutPos])
                cancel()
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    companion object {
        fun showDialogAdvanced(
            context: Context,
            onDialogAdvanceListener: OnDialogAdvanceListener
        ) {
            val dialog = DialogAdvanced(context)
            dialog.listener = onDialogAdvanceListener
            dialog.show()
        }
    }
}

interface OnDialogAdvanceListener {
    fun onDialogOk(fadeIn: Effect, fadeOut: Effect)
}


package com.example.audiocutter.functions.editor.dialogs

import android.os.Bundle
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.util.PreferencesHelper
import com.example.core.core.Effect
import kotlinx.android.synthetic.main.dialog_advanced.*

class DialogAdvanced : BaseDialog() {
    private var fadeInPos = 0
    private var fadeOutPos = 0
    private val valuesEffect = Effect.values()

    private fun initData() {
        fadeInPos = PreferencesHelper.getInt(PreferencesHelper.FADE_IN_TIME, 0)
        fadeOutPos = PreferencesHelper.getInt(PreferencesHelper.FADE_OUT_TIME, 0)

        val list = ArrayList<String>()
        for (effect in valuesEffect) {
            when (effect) {
                Effect.OFF -> {

                    list.add(getString(R.string.dialog_advanced_off))
                }
                Effect.AFTER_3_S -> {
                    list.add("${effect.time} s (${getString(R.string.dialog_advanced_recommend)})")
                }
                else -> {
                    list.add(effect.time.toString() + " s")
                }
            }
            /*if (i == 0) {
                when ()

            } else {
                val recommend = getString(R.string.dialog_advanced_recommend)
                list.add(if (valuesEffect[i].time == 3) valuesEffect[i].time.toString()
                    .plus("s ($recommend)") else valuesEffect[i].time.toString().plus("s"))
            }*/
        }

        spinner_fade_in.apply {
            setItems(list)
            selectedIndex = fadeInPos
            setOnItemSelectedListener { view, position, id, item ->
                fadeInPos = position
            }
        }
        spinner_fade_out.apply {
            setItems(list)
            selectedIndex = fadeOutPos
            setOnItemSelectedListener { view, position, id, item ->
                fadeOutPos = position
            }
        }
    }

    private fun String.upperFirstString(): String {
        val string = this.toLowerCase()
        val endString = this.substring(1, this.length).toLowerCase()
        var string1 = string.substring(0, 1).toUpperCase()
        return string1.plus(endString)
    }

    companion object {
        val TAG = "DeleteDialog"
        lateinit var dialogListener: OnDialogAdvanceListener

        @JvmStatic
        fun newInstance(listener: OnDialogAdvanceListener): DialogAdvanced {
            this.dialogListener = listener
            val dialog = DialogAdvanced()
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_advanced
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
//        initView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()

        cancel_tv.setOnClickListener {
            dialog?.dismiss()
        }
        ok_tv.setOnClickListener {
            PreferencesHelper.putInt(PreferencesHelper.FADE_IN_TIME, fadeInPos)
            PreferencesHelper.putInt(PreferencesHelper.FADE_OUT_TIME, fadeOutPos)
            dialogListener.onDialogOk(valuesEffect[fadeInPos], valuesEffect[fadeOutPos])
            dialog?.dismiss()
        }
    }
}

interface OnDialogAdvanceListener {
    fun onDialogOk(fadeIn: Effect, fadeOut: Effect)
}


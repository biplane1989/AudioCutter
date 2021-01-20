package com.example.audiocutter.functions.editor.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText


@SuppressLint("AppCompatCustomView")
class TimeEditText  /* Must use this constructor in order for the layout files to instantiate the class properly */
    (context: Context?, attrs: AttributeSet?) : EditText(context, attrs) {
    private var keyImeChangeListener: KeyImeChange? = null
    fun setKeyImeChangeListener(listener: KeyImeChange?) {
        keyImeChangeListener = listener
    }

    interface KeyImeChange {
        fun onKeyIme(keyCode: Int, event: KeyEvent?)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyImeChangeListener != null) {
            keyImeChangeListener!!.onKeyIme(keyCode, event)
        }
        return false
    }
}
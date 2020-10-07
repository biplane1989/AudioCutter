package com.example.audiocutter.ui.fragment_cut.dialog

import android.app.Dialog
import android.content.Context

class DialogAdvanced : Dialog {
    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)

}
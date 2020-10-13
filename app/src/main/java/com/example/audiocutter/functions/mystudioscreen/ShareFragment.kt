package com.example.audiocutter.functions.mystudioscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audiocutter.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_studio_bottom_sheet_share, container, false)
    }
}
package com.example.audiocutter.functions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import kotlinx.android.synthetic.main.main_screen.*

class MainScreen : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cl_mp3_cutter.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "mp3 cutter", Toast.LENGTH_SHORT).show()

        })
        cl_audio_merger.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "audio merger", Toast.LENGTH_SHORT).show()

        })
        cl_audio_mixer.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "audio mixer", Toast.LENGTH_SHORT).show()

        })
        cl_contacts.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "contacts", Toast.LENGTH_SHORT).show()

        })
        cl_my_studio.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "my studio", Toast.LENGTH_SHORT).show()

        })
        cl_flash_call.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "flash call", Toast.LENGTH_SHORT).show()

        })
    }

}
package com.example.audiocutter.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.main_screen.*
import java.io.File

class MainActivity : BaseActivity() {
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.main_screen)

    }



}
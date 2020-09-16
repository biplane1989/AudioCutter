package com.example.audiocutter.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.audiocutterscreen.AudioCutterScreen

class MainActivity : BaseActivity() {
    private val CODE_WRITE_SETTINGS_PERMISSION: Int = 1000
    lateinit var audioCutterFrg: AudioCutterScreen

    @RequiresApi(Build.VERSION_CODES.M)
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.main_screen)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_SETTINGS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_SETTINGS), 100)
        }

        audioCutterFrg = AudioCutterScreen()
        supportFragmentManager.beginTransaction().add(R.id.ln_main, audioCutterFrg).commit()
        supportFragmentManager.beginTransaction().show(audioCutterFrg).commit()
    }





}
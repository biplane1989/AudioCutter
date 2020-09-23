package com.example.audiocutter.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.audioplayer.AudioPlayerImpl
import com.example.audiocutter.functions.MainScreen
import com.example.audiocutter.functions.mystudio.OutputAudioManagerScreen
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.main_screen.*
import java.io.File

class MainActivity : BaseActivity() {
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

    }

    override fun onPostCreate() {
        super.onPostCreate()
//        val outputAudioManagerScreen = OutputAudioManagerScreen.newInstance(false)
        val outputAudioManagerScreen = OutputAudioManagerScreen()

        val fragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fl_home, outputAudioManagerScreen)
        transaction.commit()
    }

}
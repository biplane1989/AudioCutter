package com.example.audiocutter.functions.mystudio.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.functions.mystudio.Constance


class OutputActivity : AppCompatActivity() {

    val TAG = "giangtd"
    var typeAudio = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_output_screen)

        val intent = getIntent()

        if (intent?.action == Constance.NOTIFICATION_ACTION_EDITOR) {

            val TYPE_AUDIO = intent.getIntExtra(Constance.TYPE_RESULT, -1)
            typeAudio = TYPE_AUDIO

        }

        val bundle = Bundle()
        bundle.putInt("TYPE_AUDIO", typeAudio)

        val myAudioManagerScreen = MyAudioManagerScreen()
        myAudioManagerScreen.arguments = bundle

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fl_result, myAudioManagerScreen)
        fragmentTransaction.commit()
    }

    override fun onNewIntent(intent: Intent?) { // truong hop screen da duoc tao roi
        super.onNewIntent(intent)
        if (intent?.action == Constance.NOTIFICATION_ACTION_EDITOR) {
            val TYPE_AUDIO = intent.getIntExtra(Constance.TYPE_RESULT, -1)
            typeAudio = TYPE_AUDIO
        }
        val bundle = Bundle()
        bundle.putInt("TYPE_AUDIO", typeAudio)

        val myAudioManagerScreen = MyAudioManagerScreen()
        myAudioManagerScreen.arguments = bundle

        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fl_result, myAudioManagerScreen)
        fragmentTransaction.commit()
    }
}
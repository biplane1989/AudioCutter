package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.fragmentcutterscreen.AudioCutFragment
import java.io.File

class CutActivity : BaseActivity() {
    private val PATH_FOLDER: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/WeDon.m4a")
    private val PATH_FOLDER1: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/Ed Sheeran - Shape Of You [Official].mp3")

    private operator fun File.plus(separator: String): String {
        return this.absolutePath + separator
    }

    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_cut)

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        val audioCutFragment = AudioCutFragment.newInstance(PATH_FOLDER1)
        ft.add(R.id.root_view, audioCutFragment)
            .addToBackStack(AudioCutFragment::class.java.simpleName).commit()
    }

}
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
            .plus("/sample1.aac")

    private operator fun File.plus(separator: String): String {
        return this.absolutePath + separator
    }

    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_cut)

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        val audioCutFragment = AudioCutFragment.newInstance(PATH_FOLDER)
        ft.add(R.id.root_view, audioCutFragment)
            .addToBackStack(AudioCutFragment::class.java.simpleName).commit()
    }

}
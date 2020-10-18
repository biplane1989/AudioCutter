package com.example.audiocutter.activities.acttest

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.editor.screen.CuttingEditorScreen
import java.io.File

class CutActivity : BaseActivity() {
    private val PATH_FOLDER: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/WeDon.m4a")
    private val PATH_FOLDER1: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/Ed Sheeran - Shape Of You [Official].mp3")
    private val PATH_FOLDER2: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .plus("/We Don_t Talk Anymore - Charlie Puth_ Se.m4a")

    private operator fun File.plus(separator: String): String {
        return this.absolutePath + separator
    }

    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_cut)
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        val audioCutFragment = CuttingEditorScreen.newInstance(PATH_FOLDER)
        ft.add(R.id.root_view, audioCutFragment)
            .addToBackStack(CuttingEditorScreen::class.java.simpleName).commit()
    }

}
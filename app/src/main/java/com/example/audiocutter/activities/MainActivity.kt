package com.example.audiocutter.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.functions.MainScreen
import com.example.audiocutter.functions.contactscreen.contacts.ListContactAdapter
import com.example.audiocutter.functions.contactscreen.contacts.ListContactScreen
import com.example.audiocutter.functions.contactscreen.select.ListSelectAudioScreen
import com.example.audiocutter.functions.mystudioscreen.OutputAudioManagerScreen
import java.io.File

class MainActivity : BaseActivity() {
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

    }

    val fragmentManager = supportFragmentManager

    override fun onPostCreate() {
        super.onPostCreate()
//        val file = File("/storage/3736-6635/aloha.mp3")
//        val tmp = ManagerFactory.getAudioFileManager().buildAudioFile(filePath = file.absolutePath)
        runOnUI {
//         val rs =   ManagerFactory.getAudioFileManager().saveFile(tmp, Folder.TYPE_CUTTER)
//            Log.d("TAG", "onPostCreate: $rs")
////            val listContactScreen = OutputAudioManagerScreen()
//            ManagerFactory.getAudioFileManager().findAllAudioFiles()

//        val listContactScreen = MainScreen()
//            val listContactScreen = ListSelectAudioScreen()
        val listContactScreen = ListContactScreen()
//        val listSelectAudioScreen = ListSelectAudioScreen()
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fl_home, listContactScreen)
            transaction.addToBackStack(null)
            transaction.commit()
        }


    }

}
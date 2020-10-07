package com.example.audiocutter.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.MainScreen
import com.example.audiocutter.functions.contactscreen.contacts.ListContactAdapter
import com.example.audiocutter.functions.contactscreen.contacts.ListContactScreen
import com.example.audiocutter.functions.contactscreen.select.ListSelectAudioScreen
import com.example.audiocutter.functions.mystudioscreen.OutputAudioManagerScreen

class MainActivity : BaseActivity(){
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

    }

    val fragmentManager = supportFragmentManager

    override fun onPostCreate() {
        super.onPostCreate()
//        val listContactScreen = OutputAudioManagerScreen()
        val listContactScreen = MainScreen()
//        val listContactScreen = ListSelectAudioScreen()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fl_home, listContactScreen)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
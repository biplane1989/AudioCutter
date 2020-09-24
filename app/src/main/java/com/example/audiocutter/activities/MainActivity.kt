package com.example.audiocutter.activities

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.functions.contactscreen.contacts.ListContactAdapter
import com.example.audiocutter.functions.contactscreen.contacts.ListContactScreen
import com.example.audiocutter.functions.mystudioscreen.OutputAudioManagerScreen

class MainActivity : BaseActivity() {
    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

    }

    override fun onPostCreate() {
        super.onPostCreate()
//        val outputAudioManagerScreen = OutputAudioManagerScreen.newInstance(false)
        val listContactScreen = ListContactScreen()

        val fragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fl_home, listContactScreen)
        transaction.commit()
    }

}
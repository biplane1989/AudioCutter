package com.example.audiocutter.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateMutable
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.databinding.ActivityMainBinding
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.MyStudioScreen

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onPostCreate() {
        super.onPostCreate()
        viewStateManager.initState(ViewStateScreen.HOME_SCREEN)
        handleNotificationIntent(intent)
    }

    override fun createView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {

        intent?.let {
            if (it.action == Constance.NOTIFICATION_ACTION_EDITOR) {

                val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT, -1)
                if (typeAudio != -1) {
                    viewStateManager.goToMyStudioScreen(findNavController(R.id.app_nav_host_fragment), typeAudio)
                }
            }
        }
    }
}
package com.example.audiocutter.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.databinding.ActivityMainBinding
import com.example.audiocutter.functions.mystudio.Constance


class MainActivity : BaseActivity() {
    val MY_APP = "AUDIO_CUTTER"
    val IS_SETTING = "IS_SETTING"
    lateinit var binding: ActivityMainBinding
    override fun onPostCreate() {
        super.onPostCreate()

        val sharePreferences = getSharedPreferences(MY_APP, MODE_PRIVATE)
        val key = sharePreferences.getInt(IS_SETTING, 0) //0 is the default value.

        if (key == 1) {
            viewStateManager.initState(ViewStateScreen.HOME_SCREEN)
            handleNotificationIntent(intent)
            val navGraph = findNavController(R.id.app_nav_host_fragment).graph
            navGraph.startDestination = R.id.main_screen
            findNavController(R.id.app_nav_host_fragment).graph = navGraph
        } else {
            viewStateManager.initState(ViewStateScreen.SPLASH)
            handleNotificationIntent(intent)

            val newSharePreferences = getSharedPreferences(MY_APP, MODE_PRIVATE).edit()
            newSharePreferences.putInt(IS_SETTING, 1)
            newSharePreferences.apply()
        }
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
                    viewStateManager.goToMyStudioScreen(
                        findNavController(R.id.app_nav_host_fragment),
                        typeAudio
                    )
                }
            }
        }
    }
}
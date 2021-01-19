package com.example.audiocutter.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.util.Util
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.MyApplication
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.ActivityMainBinding
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.CUTTING_AUDIO_TYPE
import com.example.audiocutter.functions.resultscreen.objects.MERGING_AUDIO_TYPE
import com.example.audiocutter.functions.resultscreen.objects.MIXING_AUDIO_TYPE
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import java.util.*


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onPostCreate() {
        super.onPostCreate()
        if (PreferencesHelper.isFirstTimeToUsedApp()) {
            viewStateManager.initState(ViewStateScreen.HOME_SCREEN)
            handleNotificationIntent(intent)
            val navGraph = findNavController(R.id.app_nav_host_fragment).graph
            navGraph.startDestination = R.id.main_screen
            findNavController(R.id.app_nav_host_fragment).graph = navGraph
        } else {
            viewStateManager.initState(ViewStateScreen.SPLASH)
            handleNotificationIntent(intent)
        }
//        setLanguage()
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
            when (it.action) {
                Constance.NOTIFICATION_ACTION_EDITOR_CONVERTING -> {
                    val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT_CONVERTING, -1)
                    val notificationId = intent.getIntExtra("notificationId", -6)
                    Log.d("TAG", "MainActivity  audio type converting : " + typeAudio + " notificationId ${notificationId}")
                    gotoScreen(typeAudio)
                }

                Constance.NOTIFICATION_ACTION_EDITOR_FAIL -> {
                    val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT_FAIL, -1)
                    val notificationId = intent.getIntExtra("notificationId", -6)
                    Log.d("TAG", "MainActivity  audio type fail: " + typeAudio + " notificationId ${notificationId}")
                    gotoScreen(typeAudio)
                }

                Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_CUT -> {

                    val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT_COMPLETE_CUT, -1)
                    val notificationId = intent.getIntExtra("notificationId", -6)
                    Log.d("TAG", "MainActivity  audio type complete: " + typeAudio + " notificationId ${notificationId}")
                    gotoScreen(CUTTING_AUDIO_TYPE)
                }
                Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_MERGER -> {

                    val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT_COMPLETE_MERGER, -1)
                    val notificationId = intent.getIntExtra("notificationId", -6)
                    Log.d("TAG", "MainActivity  audio type complete: " + typeAudio + " notificationId ${notificationId}")
                    gotoScreen(MERGING_AUDIO_TYPE)
                }
                Constance.NOTIFICATION_ACTION_EDITOR_COMPLETE_MIX -> {
                    val typeAudio = intent.getIntExtra(Constance.TYPE_RESULT_COMPLETE_MIX, -1)
                    val notificationId = intent.getIntExtra("notificationId", -6)
                    Log.d("TAG", "MainActivity  audio type complete: " + typeAudio + " notificationId ${notificationId}")
                    gotoScreen(MIXING_AUDIO_TYPE)
                }
                else -> {
                    //no thing
                }
            }
        }
    }

    fun gotoScreen(typeAudio: Int) {
        if (typeAudio != -1) {
            val navigationHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.app_nav_host_fragment) as NavHostFragment?

            lifecycleScope.launchWhenResumed {
                navigationHostFragment?.childFragmentManager?.primaryNavigationFragment?.let {
                    viewStateManager.goToMyStudioScreen(findNavController(R.id.app_nav_host_fragment), typeAudio, it)

                    ManagerFactory.getAudioEditorManager()
                        .refeshNotification()     // clear notification
                }
            }
        }
    }

//    private fun setLanguage() {
//        val language: String = PreferencesHelper.getString(PreferencesHelper.APP_LANGUAGE, Utils.getDefaultLanguage())
//        Log.d("abba", "setLanguage: $language")
//
//        val myLocale = Locale(language)
//
////        Utils.updateLocale(this, myLocale)
//
//        Locale.setDefault(myLocale)
//        val conf = resources.configuration
//        conf.setLocale(myLocale)
//        resources.updateConfiguration(conf, resources.displayMetrics)
//    }
}
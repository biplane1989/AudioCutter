package com.example.audiocutter.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateScreen
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.ActivityMainBinding
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.screens.FragmentMeta
import com.example.audiocutter.functions.mystudio.screens.IMyStudioActivity

class MainActivity : BaseActivity(), IMyStudioActivity {
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

    override fun sendAction(fragmentMeta: FragmentMeta) {

        supportFragmentManager.fragments.forEach {
            sendAction(it, fragmentMeta)

        }
    }

    private fun sendAction(fragment: Fragment, fragmentMeta: FragmentMeta) {
        if(fragment is BaseFragment){
            fragment.onReceivedAction(fragmentMeta)
        }
        fragment.childFragmentManager.fragments.forEach {
            if (it != fragment) {
                sendAction(it, fragmentMeta)
            }
        }
    }
}
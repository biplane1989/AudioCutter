package com.example.audiocutter.functions.settings.screens

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.recreate
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.SettingScreenBinding
import com.example.audiocutter.functions.settings.dialogs.SetLanguageDialog
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import java.util.*


class SettingScreens : BaseFragment(), SetLanguageDialog.DialogSettingsListener {
    private lateinit var binding: SettingScreenBinding
    private lateinit var dialog: SetLanguageDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.setting_screen, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Utils.getDefaultLanguage().equals("vi")){
            binding.tvLanguageSettings.text = resources.getString(R.string.vietnam_text)
        }else{
            binding.tvLanguageSettings.text = resources.getString(R.string.english_text)
        }
    }

    private fun initViews() {
        binding.lnSetlanguage.setOnClickListener {
            dialog = SetLanguageDialog(requireContext())
            dialog.setOnCallBack(this)
            dialog.show(requireActivity().supportFragmentManager, "TAG")
        }
        binding.ivSettingScreenBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun setLanguage(item: Int) {
        when (item) {
            1 -> {
                PreferencesHelper.putString(PreferencesHelper.APP_LANGUAGE, "en")
                binding.tvLanguageSettings.text = resources.getString(R.string.english_text)
                val myLocale = Locale("en")
                Locale.setDefault(myLocale)
                val conf = resources.configuration
                conf.setLocale(myLocale)
                resources.updateConfiguration(conf, resources.displayMetrics)
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requireContext().createConfigurationContext(conf);
                } else {
                    requireContext().getResources()
                        .updateConfiguration(conf, requireContext().getResources()
                            .getDisplayMetrics());
                }*/
                requireActivity().recreate()
            }
            0 -> {
                PreferencesHelper.putString(PreferencesHelper.APP_LANGUAGE, "vi")
                val myLocale = Locale("vi")
                Locale.setDefault(myLocale)
                val conf: Configuration = resources.configuration
                conf.setLocale(myLocale)
                resources.updateConfiguration(conf, resources.displayMetrics)
          /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requireContext().createConfigurationContext(conf);
                } else {
                    resources.updateConfiguration(conf, resources.displayMetrics)
                }*/
                binding.tvLanguageSettings.text = resources.getString(R.string.vietnam_text)
                requireActivity().recreate()
            }
        }
    }


}
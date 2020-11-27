package com.example.audiocutter.functions.settings.screens

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.SettingScreenBinding
import com.example.audiocutter.functions.settings.dialogs.SetLanguageDialog
import java.util.*


class SettingScreens : BaseFragment(), SetLanguageDialog.DialogSettingsListener {
    private lateinit var binding: SettingScreenBinding
    private lateinit var dialog: SetLanguageDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.setting_screen, container, false)
        initViews()
        return binding.root
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
                binding.tvLanguageSettings.text = resources.getString(R.string.english_text)

                val res = requireContext().resources
                val dm: DisplayMetrics = res.displayMetrics
                val conf: Configuration = res.configuration
                conf.setLocale(Locale("Vi")) // API 17+ only.
                res.updateConfiguration(conf, dm)
            }
            0 -> {
                binding.tvLanguageSettings.text = resources.getString(R.string.vietnam_text)
            }
        }
    }


}
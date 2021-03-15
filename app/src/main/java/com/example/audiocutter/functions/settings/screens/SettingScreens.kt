package com.example.audiocutter.functions.settings.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.activities.MainActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.result.AudioEditorManagerlmpl
import com.example.audiocutter.databinding.SettingScreenBinding
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.services.ResultService
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
        if (Utils.getDefaultLanguage().equals("vi")) {
            binding.tvLanguageSettings.text = resources.getString(R.string.vietnam_text)
        } else {
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
                val myLocale = Locale("en", "US")

                Utils.updateLocale(requireContext(), myLocale)

                binding.tvLanguageSettings.text = resources.getString(R.string.english_text)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requireActivity().recreate()
                } else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().onBackPressed()
                }

                if (AudioEditorManagerlmpl.isMyServiceRunning(ResultService::class.java)) {
                    val intent = Intent(requireContext(), ResultService::class.java)
                    intent.setAction(Constance.SERVICE_ACTION_CHANGE_LANGUAGE)
                    requireActivity().startService(intent)
                    Log.d("TAG", "setLanguage: onDestroy: service 1")
                }
            }
            0 -> {
                PreferencesHelper.putString(PreferencesHelper.APP_LANGUAGE, "vi")
                val myLocale = Locale("vi", "VN")

                Utils.updateLocale(requireContext(), myLocale)

                binding.tvLanguageSettings.text = resources.getString(R.string.vietnam_text)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requireActivity().recreate()
                } else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().onBackPressed()
                }

                if (AudioEditorManagerlmpl.isMyServiceRunning(ResultService::class.java)) {
                    val intent = Intent(requireContext(), ResultService::class.java)
                    intent.setAction(Constance.SERVICE_ACTION_CHANGE_LANGUAGE)
                    requireActivity().startService(intent)
                    Log.d("TAG", "setLanguage: onDestroy: service 2")
                }
            }
        }
    }


}
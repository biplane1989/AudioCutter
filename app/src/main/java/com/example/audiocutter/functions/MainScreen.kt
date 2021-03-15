package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MainScreenBinding
import com.example.audiocutter.functions.common.ContactPermissionDialog
import com.example.audiocutter.functions.common.StoragePermissionDialog
import com.example.audiocutter.functions.flashcall.dialogs.PhoneCallPerMissionDialog
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.Constance.PERMISSION_CONTACTS_SCREEN
import com.example.audiocutter.functions.mystudio.Constance.PERMISSION_CUT_SCREEN
import com.example.audiocutter.functions.mystudio.Constance.PERMISSION_MERGER_SCREEN
import com.example.audiocutter.functions.mystudio.Constance.PERMISSION_MIX_SCREEN
import com.example.audiocutter.permissions.*
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.REQUEST_CODE
import com.example.audiocutter.util.REQUEST_CODE_2
import com.example.audiocutter.util.Utils.Companion.permissionsContactScreen
import com.example.audiocutter.util.Utils.Companion.permissionsCutterScreen
import com.example.audiocutter.util.Utils.Companion.permissionsMergerScreen
import com.example.audiocutter.util.Utils.Companion.permissionsMixScreen
import com.example.audiocutter.util.permissions
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.hasPermissions
import pub.devrel.easypermissions.onRequestPermissionsResulted
import pub.devrel.easypermissions.requestPermission

class MainScreen : BaseFragment(), View.OnClickListener {
    private val MP3_CUTTER_REQUESTING_PERMISSION = 1 shl 1
    private val AUDIO_MERGER_REQUESTING_PERMISSION = 1 shl 2
    private val AUDIO_MIXER_REQUESTING_PERMISSION = 1 shl 3
    private val CONTACTS_ITEM_REQUESTING_PERMISSION = 1 shl 4
    private val MY_STUDIO_REQUESTING_PERMISSION = 1 shl 5
    private val FLASH_CALL_REQUESTING_PERMISSION = 1 shl 6
    private val FIRST_TIME_TO_USED_APP_REQUESTING_PERMISSION = 1 shl 7



    private lateinit var binding: MainScreenBinding
    private var pendingRequestingPermission = 0
    private val TAG = "giangtd"
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (PreferencesHelper.isFirstTimeToUsedApp()) {
            lifecycleScope.launchWhenResumed {
                if (!storagePermissionRequest.isPermissionGranted()) {
                    pendingRequestingPermission = FIRST_TIME_TO_USED_APP_REQUESTING_PERMISSION
                    storagePermissionRequest.requestPermission()
                }
            }
        }
        PreferencesHelper.setFirstTimeToUsedApp(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_screen, container, false)
        lifecycleScope.launchWhenResumed {
            binding.btnVip.startBlink()
        }
        lifecycleScope.launchWhenResumed { binding.advertisementButton.startAnim() }

          PermissionManager.getAppPermission()
              .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                  if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and FIRST_TIME_TO_USED_APP_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      ManagerFactory.getAudioFileManager().init(requireContext())
                  }
                  if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and MP3_CUTTER_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      onMp3CutterItemClicked()
                  }
                  if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and AUDIO_MERGER_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      onAudioMergerItemClicked()
                  }
                  if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and AUDIO_MIXER_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      onAudioMixerItemClicked()
                  }
                  if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and MY_STUDIO_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      viewStateManager.mainScreenOnMyAudioItemClicked(this)
                      onMyStudioItemClicked()
                  }
                  if (writeSettingPermissionRequest.isPermissionGranted()) {
                      if ((pendingRequestingPermission and MY_STUDIO_REQUESTING_PERMISSION) != 0) {
                          resetRequestingPermission()
                          if (contactPermissionRequest.isPermissionGranted()) {
                              onMyStudioItemClicked()
                          } else {
                              pendingRequestingPermission = MY_STUDIO_REQUESTING_PERMISSION
                              contactPermissionRequest.requestPermission()
                          }
                      }
                      if ((pendingRequestingPermission and MP3_CUTTER_REQUESTING_PERMISSION) != 0) {
                          onMp3CutterItemClicked()
                      }

                  }
                  if (callPhonePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and FLASH_CALL_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      onFlashCallItemClicked()
                  }

                  if (contactPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and CONTACTS_ITEM_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      onContactsItemClicked()
                  }
              })

        return binding.root
    }

    override fun onClick(view: View) {
        when (view) {
            binding.mp3CutterItemCl -> {
                onMp3CutterItemClicked()
            }
            binding.audioMergerItemCl -> {
                onAudioMergerItemClicked()
            }
            binding.audioMixerItemCl -> {
                onAudioMixerItemClicked()
            }
            binding.contactItemCl -> {
                onContactsItemClicked()
            }
            binding.myStudioItemCl -> {
                onMyStudioItemClicked()
            }
            binding.flashCallItemCl -> {
                onFlashCallItemClicked()
            }
            binding.settingButton -> {

                viewStateManager.mainScreenOnItemClickSetting(this)
            }
            binding.advertisementButton -> {
            }
            binding.btnVip -> {

            }
        }
    }

    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }

    @AfterPermissionGranted(Constance.PERMISSION_CUT_SCREEN)
    fun startCutterScreen() {
        ManagerFactory.getAudioFileManager().init(requireContext())
        viewStateManager.mainScreenOnMp3CutItemClicked(this)
    }

    private fun onMp3CutterItemClicked() {
//        if (storagePermissionRequest.isPermissionGranted()) {
//            ManagerFactory.getAudioFileManager().init(requireContext())
//            viewStateManager.mainScreenOnMp3CutItemClicked(this)
//        } else {
        if (!this.hasPermissions(permissionsCutterScreen)) {
            StoragePermissionDialog.newInstance {
//                resetRequestingPermission()
//                pendingRequestingPermission = MP3_CUTTER_REQUESTING_PERMISSION
//                storagePermissionRequest.requestPermission()
                this.requestPermission(PERMISSION_CUT_SCREEN, permissionsCutterScreen)
            }
                .show(requireActivity().supportFragmentManager, StoragePermissionDialog::class.java.name)
        }else{
            this.requestPermission(PERMISSION_CUT_SCREEN, permissionsCutterScreen)
        }

//        }
    }

    @AfterPermissionGranted(Constance.PERMISSION_MERGER_SCREEN)
    fun startMergerScreen() {
        ManagerFactory.getAudioFileManager().init(requireContext())
        viewStateManager.mainScreenOnMp3MergeItemClicked(this)
    }

    private fun onAudioMergerItemClicked() {
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMp3MergeItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
//                resetRequestingPermission()
//                pendingRequestingPermission = AUDIO_MERGER_REQUESTING_PERMISSION
//                storagePermissionRequest.requestPermission()

                this.requestPermission(PERMISSION_MERGER_SCREEN, permissionsMergerScreen)
            }
                .show(requireActivity().supportFragmentManager, StoragePermissionDialog::class.java.name)
        }
    }

    @AfterPermissionGranted(Constance.PERMISSION_MIX_SCREEN)
    fun startMixScreen() {
        ManagerFactory.getAudioFileManager().init(requireContext())
        viewStateManager.mainScreenOnMp3MixItemClicked(this)
    }

    private fun onAudioMixerItemClicked() {
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMp3MixItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
//                resetRequestingPermission()
//                pendingRequestingPermission = AUDIO_MIXER_REQUESTING_PERMISSION
//                storagePermissionRequest.requestPermission()

                this.requestPermission(PERMISSION_MIX_SCREEN, permissionsMixScreen)
            }
                .show(requireActivity().supportFragmentManager, StoragePermissionDialog::class.java.name)
        }
    }


    @AfterPermissionGranted(Constance.PERMISSION_CONTACTS_SCREEN)
    fun startContactScreen() {
        ManagerFactory.getAudioFileManager().init(requireContext())
        viewStateManager.mainScreenOnContactItemClicked(this)
    }

    private fun onContactsItemClicked() {
        if (contactPermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnContactItemClicked(this)
        } else {
            ContactPermissionDialog.newInstance {
//                resetRequestingPermission()
//                pendingRequestingPermission = CONTACTS_ITEM_REQUESTING_PERMISSION
//                contactPermissionRequest.requestPermission()
                this.requestPermission(PERMISSION_CONTACTS_SCREEN, permissionsContactScreen)

            }
                .show(requireActivity().supportFragmentManager, ContactPermissionDialog::class.java.name)
        }
    }

    private fun onMyStudioItemClicked() {

/*        if (contactPermissionRequest.isPermissionGranted() && writeSettingPermissionRequest.isPermissionGranted()) {
            viewStateManager.mainScreenOnMyAudioItemClicked(this)
        } else {*/
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMyAudioItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = MY_STUDIO_REQUESTING_PERMISSION
                storagePermissionRequest.requestPermission()
            }
                .show(requireActivity().supportFragmentManager, StoragePermissionDialog::class.java.name)

        }
    }

    private fun checkAndRequestStorageAndSettingPermission() {
        if (!contactPermissionRequest.isPermissionGranted()) {
            contactPermissionRequest.requestPermission()
        } else {
            writeSettingPermissionRequest.requestPermission()
        }
    }

    private fun onFlashCallItemClicked() {
        if (callPhonePermissionRequest.isPermissionGranted()) {
            viewStateManager.mainScreenOnFlashCallItemClicked(this)
        } else {
            PhoneCallPerMissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = FLASH_CALL_REQUESTING_PERMISSION
                callPhonePermissionRequest.requestPermission()
            }
                .show(requireActivity().supportFragmentManager, PhoneCallPerMissionDialog::class.java.name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mp3CutterItemCl.setOnClickListener(this)
        binding.audioMergerItemCl.setOnClickListener(this)
        binding.audioMixerItemCl.setOnClickListener(this)
        binding.contactItemCl.setOnClickListener(this)
        binding.myStudioItemCl.setOnClickListener(this)
        binding.flashCallItemCl.setOnClickListener(this)
        binding.settingButton.setOnClickListener(this)
        binding.advertisementButton.setOnClickListener(this)
        binding.btnVip.setOnClickListener(this)
    }

    private val storagePermissionRequest = object : StoragePermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }
    private val contactPermissionRequest = object : ContactItemPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    private val writeSettingPermissionRequest = object : WriteSettingPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    private val callPhonePermissionRequest = object : FlashCallPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    init {
        storagePermissionRequest.init()
        contactPermissionRequest.init()
        writeSettingPermissionRequest.init()
        callPhonePermissionRequest.init()
    }

}
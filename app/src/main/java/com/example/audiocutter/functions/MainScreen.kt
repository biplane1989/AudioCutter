package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MainScreenBinding
import com.example.audiocutter.functions.common.ContactPermissionDialog
import com.example.audiocutter.functions.common.StoragePermissionDialog
import com.example.audiocutter.permissions.*

class MainScreen : BaseFragment(), View.OnClickListener {
    private val MP3_CUTTER_REQUESTING_PERMISSION = 1 shl 1
    private val AUDIO_MERGER_REQUESTING_PERMISSION = 1 shl 2
    private val AUDIO_MIXER_REQUESTING_PERMISSION = 1 shl 3
    private val CONTACTS_ITEM_REQUESTING_PERMISSION = 1 shl 4
    private val MY_STUDIO_REQUESTING_PERMISSION = 1 shl 5
    private val FLASH_CALL_REQUESTING_PERMISSION = 1 shl 6

    private lateinit var binding: MainScreenBinding
    private var pendingRequestingPermission = 0
    private val TAG = "giangtd"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_screen, container, false)
        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and MP3_CUTTER_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    onMp3CutterItemClicked()
                }
                if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and AUDIO_MERGER_REQUESTING_PERMISSION) != 0) {
                    onAudioMergerItemClicked()
                    onMp3CutterItemClicked()
                }
                if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and AUDIO_MIXER_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    onAudioMixerItemClicked()
                }
                if (storagePermissionRequest.isPermissionGranted() && (pendingRequestingPermission and MY_STUDIO_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    if (writeSettingPermissionRequest.isPermissionGranted()) {
                        onMyStudioItemClicked()
                    } else {
                        pendingRequestingPermission = MY_STUDIO_REQUESTING_PERMISSION
                        writeSettingPermissionRequest.requestPermission()
                    }

                }
                if (writeSettingPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and MY_STUDIO_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    if (contactPermissionRequest.isPermissionGranted()) {
                        onMyStudioItemClicked()
                    } else {
                        pendingRequestingPermission = MY_STUDIO_REQUESTING_PERMISSION
                        contactPermissionRequest.requestPermission()
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
        }
    }

    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }

    private fun onMp3CutterItemClicked() {
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMp3CutItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = MP3_CUTTER_REQUESTING_PERMISSION
                storagePermissionRequest.requestPermission()
            }
                .show(
                    requireActivity().supportFragmentManager,
                    StoragePermissionDialog::class.java.name
                )
        }
    }

    private fun onAudioMergerItemClicked() {
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMp3MergeItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = AUDIO_MERGER_REQUESTING_PERMISSION
                storagePermissionRequest.requestPermission()
            }
                .show(
                    requireActivity().supportFragmentManager,
                    StoragePermissionDialog::class.java.name
                )
        }
    }

    private fun onAudioMixerItemClicked() {
        if (storagePermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMp3MixItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = AUDIO_MIXER_REQUESTING_PERMISSION
                storagePermissionRequest.requestPermission()
            }
                .show(
                    requireActivity().supportFragmentManager,
                    StoragePermissionDialog::class.java.name
                )
        }
    }

    private fun onContactsItemClicked() {
        if (contactPermissionRequest.isPermissionGranted()) {
            viewStateManager.mainScreenOnContactItemClicked(this)
        } else {
            ContactPermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = CONTACTS_ITEM_REQUESTING_PERMISSION
                contactPermissionRequest.requestPermission()
            }
                .show(
                    requireActivity().supportFragmentManager,
                    ContactPermissionDialog::class.java.name
                )
        }
    }

    private fun onMyStudioItemClicked() {
        if (contactPermissionRequest.isPermissionGranted() && writeSettingPermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            viewStateManager.mainScreenOnMyAudioItemClicked(this)
        } else {
            StoragePermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = MY_STUDIO_REQUESTING_PERMISSION
                if (!contactPermissionRequest.isPermissionGranted()) {
                    contactPermissionRequest.requestPermission()
                } else {
                    writeSettingPermissionRequest.requestPermission()
                }
            }
                .show(
                    requireActivity().supportFragmentManager,
                    StoragePermissionDialog::class.java.name
                )

        }
    }

    private fun onFlashCallItemClicked() {
        if (callPhonePermissionRequest.isPermissionGranted()) {
            viewStateManager.mainScreenOnFlashCallItemClicked(this)
        } else {
            pendingRequestingPermission = FLASH_CALL_REQUESTING_PERMISSION
            callPhonePermissionRequest.requestPermission()
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
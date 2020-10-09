package com.example.audiocutter.functions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.MainScreenBinding
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.permissions.StoragePermissionRequest
import kotlinx.android.synthetic.main.main_screen.*

class MainScreen : BaseFragment(), View.OnClickListener {
    private val MP3_CUTTER_REQUESTING_PERMISSION = 1 shl 1
    private val AUDIO_MERGER_REQUESTING_PERMISSION = 1 shl 2
    private val AUDIO_MIXER_REQUESTING_PERMISSION = 1 shl 3
    private val CONTACTS_REQUESTING_PERMISSION = 1 shl 4
    private val MY_STUDIO_REQUESTING_PERMISSION = 1 shl 5
    private val FLASH_CALL_REQUESTING_PERMISSION = 1 shl 6
    private lateinit var binding: MainScreenBinding
    private var pendingRequestingPermission = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.main_screen, container, false)
        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (it.hasStoragePermission() && (pendingRequestingPermission and MP3_CUTTER_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    onMp3CutterItemClicked()
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

            }
            binding.audioMixerItemCl -> {

            }
            binding.contactItemCl -> {

            }
            binding.myStudioItemCl -> {

            }
            binding.flashCallItemCl -> {

            }
            binding.settingButton -> {
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
            Toast.makeText(requireContext(), "onMp3CutterItemClicked", Toast.LENGTH_SHORT).show()
        } else {
            resetRequestingPermission()
            pendingRequestingPermission = MP3_CUTTER_REQUESTING_PERMISSION
            storagePermissionRequest.requestPermission()
        }
    }

    private fun onAudioMergerItemClicked() {

    }

    private fun onAudioMixerItemClicked() {

    }

    private fun onContactsItemClicked() {

    }

    private fun onMyStudioItemClicked() {

    }

    private fun onFlashCallItemClicked() {

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

    init {
        storagePermissionRequest.init()
    }


}
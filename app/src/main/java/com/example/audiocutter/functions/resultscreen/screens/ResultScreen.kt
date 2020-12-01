package com.example.audiocutter.functions.resultscreen.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.ResultScreenBinding
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.functions.common.ContactPermissionDialog
import com.example.audiocutter.functions.mystudio.dialog.CancelDialog
import com.example.audiocutter.functions.mystudio.dialog.CancelDialogListener
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.ContactItemPermissionRequest
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat


class ResultScreen : BaseFragment(), View.OnClickListener, CancelDialogListener, DialogAppShare.DialogAppListener {
    companion object {
        const val MIX = 3
        const val MER = 2
        const val CUT = 1
    }

    private var audioFile: AudioFile? = null
    private lateinit var dialogAppShare: DialogAppShare
    private var isLoadingDone = false
    private val safeArg: ResultScreenArgs by navArgs()
    private val TAG = "giangtd"
    private lateinit var binding: ResultScreenBinding
    private lateinit var mResultViewModel: ResultViewModel
    private var isSeekBarStatus = false

    @SuppressLint("SimpleDateFormat")
    private var simpleDateFormat = SimpleDateFormat("mm:ss")
    private var pendingRequestingPermission = 0
    private val CONTACTS_ITEM_REQUESTING_PERMISSION = 1 shl 4
    private val contactPermissionRequest = object : ContactItemPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    private val processDoneObserver = Observer<AudioFile> {         // observer trang thai done

        binding.btnBack.visibility = View.VISIBLE
        binding.ivHome.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.INVISIBLE
        binding.tvWait.visibility = View.GONE
        binding.llProgressbar.visibility = View.GONE
        binding.llPlayMusic.visibility = View.VISIBLE
        binding.clOpption.visibility = View.VISIBLE
        binding.btnOrigin.visibility = View.GONE
        binding.tvTitleResult.visibility = View.VISIBLE
        binding.tvTitleLoading.visibility = View.GONE

        isLoadingDone = true
        it?.let {
            context?.let { context ->
                binding.tvTimeLife.width = Utils.getWidthText(simpleDateFormat.format(it.duration), context)
                    .toInt() + 15
            }
            binding.tvTitleMusic.text = it.fileName
            binding.tvInfoMusic.text = String.format("%s kb/s", (it.bitRate / 1000).toString())

            binding.tvTimeTotal.text = String.format("/%s", simpleDateFormat.format(it.duration.toInt()))
            binding.tvInfoMusic.setText(convertAudioSizeToString(it))
        }
        val cancelDialog = childFragmentManager.findFragmentByTag(CancelDialog.TAG)
        if (cancelDialog is CancelDialog) {
            cancelDialog.dismiss()
        }
    }
    private val pendingProcessObserver = Observer<String> {     // observer trang thai pending
        binding.tvWait.visibility = View.VISIBLE
        binding.llProgressbar.visibility = View.GONE
        binding.llPlayMusic.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.btnBack.visibility = View.GONE
        binding.ivHome.visibility = View.INVISIBLE
        binding.btnCancel.visibility = View.VISIBLE

    }

    @SuppressLint("SetTextI18n")
    private val processingObserver = Observer<ConvertingItem> {     // observer trang thai processing

        binding.btnOrigin.visibility = View.VISIBLE
        binding.btnBack.visibility = View.GONE
        binding.ivHome.visibility = View.INVISIBLE
        binding.tvWait.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.llProgressbar.visibility = View.VISIBLE
        binding.llPlayMusic.visibility = View.GONE
        binding.btnCancel.visibility = View.VISIBLE
        binding.pbLoading.progress = it.percent
        binding.tvLoading.visibility = View.VISIBLE
        binding.tvLoading.text = it.percent.toString() + "%"
        binding.tvTitleMusic.text = it.getFileName()
        binding.tvInfoMusic.text = it.bitRate.toString() + resources.getString(R.string.kbps_result)
        binding.ivNotificationError.visibility = View.GONE

        isLoadingDone = false
    }

    private val errorObserver = Observer<Boolean> {
        if (it) {
            view?.let {
                val mySnackbar = Snackbar.make(it, getString(R.string.result_screen_converting_error), Snackbar.LENGTH_LONG)
                mySnackbar.show()
            }
        }

        binding.btnOrigin.visibility = View.GONE
        binding.tvWait.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.llProgressbar.visibility = View.VISIBLE
        binding.llPlayMusic.visibility = View.GONE
        binding.tvLoading.visibility = View.GONE

        binding.btnBack.visibility = View.VISIBLE
        binding.ivHome.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.INVISIBLE

        binding.ivNotificationError.visibility = View.VISIBLE
        isLoadingDone = false
    }

    private fun convertAudioSizeToString(audioFile: AudioFile): String {
        var formatSize = "MB"
        var sizeValue = 0f
        if (audioFile.size < (1024 * 1024)) {
            formatSize = "KB"
            sizeValue = audioFile.size / 1024f
        } else {
            sizeValue = audioFile.size / (1024f * 1024)
        }

        val str = StringBuilder()
        str.append(String.format("%.1f", sizeValue))
        str.append(" ")
        str.append(formatSize)
        str.append(" | ")
        str.append((audioFile.bitRate /1000).toString())
        str.append("kb/s")
        return str.toString()
    }

    @SuppressLint("SetTextI18n")
    val playInfoObserver = Observer<PlayerInfo> { playInfo ->       // observer info play music

        if (!isSeekBarStatus) {
            binding.sbMusic.max = playInfo.duration
            binding.sbMusic.progress = playInfo.posision
            binding.tvTimeTotal.text = "/" + simpleDateFormat.format(playInfo.duration)
            binding.tvTimeLife.text = simpleDateFormat.format(playInfo.posision)

            when (playInfo.playerState) {
                PlayerState.IDLE -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                }
                PlayerState.PAUSE -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                }
                PlayerState.PLAYING -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
                }
                else -> {
                    //nothing
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)
        mResultViewModel.getPendingProcessLiveData()
            .observe(viewLifecycleOwner, pendingProcessObserver)
        mResultViewModel.getProcessingLiveData().observe(viewLifecycleOwner, processingObserver)
        mResultViewModel.getProcessDoneLiveData().observe(viewLifecycleOwner, processDoneObserver)
        mResultViewModel.getPlayerInfo().observe(viewLifecycleOwner, playInfoObserver)
        mResultViewModel.getErrorLiveData().observe(viewLifecycleOwner, errorObserver)
        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (contactPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and CONTACTS_ITEM_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    viewStateManager.resultScreenSetContactItemClicked(
                        this,
                        audioFile!!.file.absolutePath
                    )
                }

            })
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mResultViewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)

        mResultViewModel.init(safeArg)      // nhan data tu fragment truyen sang va editor audio
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbLoading.max = 100

        binding.ivPausePlayMusic.setOnClickListener(this)
        binding.llRingtone.setOnClickListener(this)
        binding.llAlarm.setOnClickListener(this)
        binding.llNotification.setOnClickListener(this)
        binding.llShare.setOnClickListener(this)
        binding.llContact.setOnClickListener(this)
        binding.llOpenwith.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.ivHome.setOnClickListener(this)
        binding.btnOrigin.setOnClickListener(this)

        binding.sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.tvTimeLife.text = simpleDateFormat.format(binding.sbMusic.progress)             // update time cho tvTimeLife khi keo seekbar
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isSeekBarStatus = true
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                isSeekBarStatus = false
                mResultViewModel.seekToAudio(sb!!.progress)
            }
        })
    }


    override fun onClick(view: View?) {
        audioFile = ManagerFactory.getAudioEditorManager()
            .getLatestConvertingItem()?.outputAudioFile
        when (view) {
            binding.ivPausePlayMusic -> {
                runOnUI {
                    Log.d("001", "onClick: ")
                    mResultViewModel.playAudio()
                }
            }

            binding.btnCancel -> {
                if (childFragmentManager.findFragmentByTag(CancelDialog.TAG) == null) {
                    ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.let {
                        val dialog = CancelDialog.newInstance(this, it.id)
                        dialog.show(childFragmentManager, CancelDialog.TAG)
                    }
                }
            }

            binding.btnBack -> {
                requireActivity().onBackPressed()
            }

            binding.ivHome -> {
                viewStateManager.resultScreenGoToHome(this)
            }

            binding.btnOrigin -> {
                viewStateManager.resultScreenGoToHome(this)
            }
            binding.llRingtone -> {
                if (mResultViewModel.setRingTone()) {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_ringtone_successful), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_ringtone_fail), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.llAlarm -> {

                if (mResultViewModel.setAlarm()) {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_alarm_successful), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_alarm_fail), Toast.LENGTH_SHORT)
                        .show()
                }

            }

            binding.llNotification -> {
                if (mResultViewModel.setNotification()) {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_notification_successful), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.result_screen_set_notification_fail), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.llShare -> {
                ShowDialogShareFile()

            }
            binding.llContact -> {
                audioFile?.let {
                   checkPermissionContact()
                }

            }
            binding.llOpenwith -> {
                audioFile?.let {
                    Utils.openWithApp(requireContext(), audioFile!!.uri!!)
                }
            }
        }
    }

    private fun checkPermissionContact() {
        if (contactPermissionRequest.isPermissionGranted()) {
            viewStateManager.resultScreenSetContactItemClicked(
                this,
                audioFile!!.file.absolutePath
            )
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


    private fun ShowDialogShareFile() {
        dialogAppShare = DialogAppShare(requireContext())
        dialogAppShare.setOnCallBack(this)
        dialogAppShare.show(requireActivity().supportFragmentManager, "TAG_DIALOG")
    }

    override fun shareFileAudioToAppDevices() {
        dialogAppShare.dismiss()
        Utils.shareFileAudio(requireContext(), audioFile!!)
    }

    override fun shareFilesToAppsDialog(pkgName: String) {
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, audioFile!!.uri)
        intent.type = "audio/*"
        intent.`package` = pkgName
        intent.action = Intent.ACTION_SEND
        startActivity(intent)
    }


    override fun onCancelDeleteClick(id: Int) {             // interface delete dialog
        ManagerFactory.getAudioEditorManager().cancel(id)
        requireActivity().onBackPressed()
    }

    override fun onCancelDialog() {
        // no thing
    }


    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }


    override fun onResume() {       // xu ly back button
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (!isLoadingDone) {
                    if (childFragmentManager.findFragmentByTag(CancelDialog.TAG) == null) {                     // xu ly double click button
                        ManagerFactory.getAudioEditorManager().getLatestConvertingItem()?.let {
                            val dialog = CancelDialog.newInstance(this@ResultScreen, it.id)
                            dialog.show(childFragmentManager, CancelDialog.TAG)
                        }
                    }
                } else {
                    requireActivity().onBackPressed()
                }
                // handle back button
                true
            } else false
        }
    }
}
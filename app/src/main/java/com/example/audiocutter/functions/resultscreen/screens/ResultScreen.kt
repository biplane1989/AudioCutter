package com.example.audiocutter.functions.resultscreen.screens

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.ResultScreenBinding
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.functions.audiochooser.dialogs.TypeShare
import com.example.audiocutter.functions.common.ContactPermissionDialog
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.dialog.CancelDialog
import com.example.audiocutter.functions.mystudio.dialog.CancelDialogListener
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.ContactItemPermissionRequest
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.permissions.WriteSettingPermissionRequest
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


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
    private var numberClick = 0
    private var sbAnimation: ObjectAnimator? = null
    private var progressbarAnimation: ObjectAnimator? = null
    private var playerState: PlayerState = PlayerState.IDLE
    private var timeFomat = 0
//    private var toast: Toast? = null

    private var pendingRequestingPermission = 0
    private val CONTACTS_ITEM_REQUESTING_PERMISSION = 1 shl 4
    private val WRITESETTING_ITEM_REQUESTING_PERMISSION = 1 shl 5
    private val DURATION_ANIMATION = 500L

    private var isFirstPlayMusic = true

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

    private val lastItemObserver = Observer<ConvertingItem> {

        binding.tvTitleMusic.text = it.getFileName()
        binding.tvInfoMusic.text = String.format("%s kb/s", (it.bitRate).toString())
    }

    @SuppressLint("SetTextI18n")
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

            timeFomat = Utils.chooseTimeFormat(it.duration.toLong())

            binding.tvTitleMusic.text = it.fileName
            binding.tvInfoMusic.text = String.format("%s kb/s", (it.bitRate / 1000).toString())

            binding.tvTimeLife.width = binding.tvTimeTotal.paint.measureText(Utils.toTimeStr(it.duration, timeFomat))
                .toInt()
            binding.tvTimeTotal.width = binding.tvTimeLife.paint.measureText("/" + Utils.toTimeStr(it.duration, timeFomat))
                .toInt()

            binding.sbMusic.max = (it.duration * 100).toInt()
            binding.tvTimeTotal.text = "/" + Utils.toTimeStr(it.duration.toLong(), timeFomat)
            binding.tvInfoMusic.text = convertAudioSizeToString(it)
        }
        val cancelDialog = childFragmentManager.findFragmentByTag(CancelDialog.TAG)
        if (cancelDialog is CancelDialog) {
            cancelDialog.dismiss()
        }
        //progressbarAnimation?.cancel()
        Log.d(TAG, "status process done : ")
    }
    private val pendingProcessObserver = Observer<String> {     // observer trang thai pending
        binding.tvWait.visibility = View.VISIBLE
        binding.llProgressbar.visibility = View.GONE
        binding.llPlayMusic.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.btnBack.visibility = View.GONE
        binding.ivHome.visibility = View.INVISIBLE
        binding.btnCancel.visibility = View.VISIBLE
        binding.tvTitleResult.visibility = View.GONE
        binding.tvTitleLoading.visibility = View.VISIBLE

        binding.pbLoading.clearAnimation()      // reset progressbar
        progressbarAnimation?.cancel()
        binding.pbLoading.progress = 0

        setProgressAnimate(binding.pbLoading, 0, DURATION_ANIMATION)
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
        binding.tvLoading.visibility = View.VISIBLE
        binding.tvLoading.text = it.percent.toString() + "%"
        binding.tvTitleMusic.text = it.getFileName()
        binding.tvInfoMusic.text = it.bitRate.toString() + resources.getString(R.string.kbps_result)
        binding.ivNotificationError.visibility = View.GONE

        binding.tvTitleResult.visibility = View.GONE
        binding.tvTitleLoading.visibility = View.VISIBLE

//        binding.pbLoading.progress = it.percent

        if (isFirstPlayMusic) {
            binding.pbLoading.clearAnimation()      // reset progressbar
            progressbarAnimation?.cancel()
            binding.pbLoading.progress = it.percent * 100
        } else {
            setProgressAnimate(binding.pbLoading, it.percent, DURATION_ANIMATION)
            isFirstPlayMusic = false
        }


        isLoadingDone = false
    }

    private val errorObserver = Observer<Boolean> {
        binding.btnOrigin.visibility = View.GONE
        binding.tvWait.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.llProgressbar.visibility = View.VISIBLE
        binding.llPlayMusic.visibility = View.GONE
        binding.tvLoading.visibility = View.GONE

        binding.btnBack.visibility = View.VISIBLE
        binding.ivHome.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.INVISIBLE

        binding.tvTitleResult.visibility = View.GONE
        binding.tvTitleLoading.visibility = View.VISIBLE

        binding.ivNotificationError.visibility = View.VISIBLE
        isLoadingDone = false

        progressbarAnimation?.cancel()
        Log.d(TAG, "status process : ")
    }

    private val cancelObserver = Observer<Boolean>{
        binding.btnOrigin.visibility = View.GONE
        binding.tvWait.visibility = View.GONE
        binding.clOpption.visibility = View.GONE
        binding.llProgressbar.visibility = View.GONE
        binding.llPlayMusic.visibility = View.GONE
        binding.tvLoading.visibility = View.GONE

        binding.btnBack.visibility = View.VISIBLE
        binding.ivHome.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.INVISIBLE

        binding.tvTitleResult.visibility = View.GONE
        binding.tvTitleLoading.visibility = View.VISIBLE

        binding.ivNotificationError.visibility = View.GONE
        isLoadingDone = false

        progressbarAnimation?.cancel()
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
        str.append((audioFile.bitRate / 1000).toString())
        str.append("kb/s")
        return str.toString()
    }

    @SuppressLint("SetTextI18n")
    val playInfoObserver = Observer<PlayerInfo> { playInfo ->       // observer info play music
        playerState = playInfo.playerState
        if (!isSeekBarStatus) {
            timeFomat = Utils.chooseTimeFormat(playInfo.duration.toLong())
            binding.tvTimeLife.text = Utils.toTimeStr(playInfo.posision.toLong(), timeFomat)

            when (playInfo.playerState) {
                PlayerState.IDLE -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                    binding.tvTimeLife.text = Constance.TIME_LIFE_DEFAULT
                    binding.sbMusic.clearAnimation()
                    sbAnimation?.cancel()
                    binding.sbMusic.progress = 0
                    setSeekbarAnimate(binding.sbMusic, 0, DURATION_ANIMATION)
                }
                PlayerState.PAUSE -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                }
                PlayerState.PLAYING -> {
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_pause)
                    setSeekbarAnimate(binding.sbMusic, playInfo.posision, DURATION_ANIMATION)
                }
                else -> {
                    //nothing
                }
            }
        }
    }

    fun setSeekbarAnimate(pb: SeekBar, progressTo: Int, duration: Long) {
        // smooth animation
        sbAnimation?.cancel()
        sbAnimation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
        sbAnimation?.duration = duration
        sbAnimation?.interpolator = DecelerateInterpolator()
        sbAnimation?.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)
        mResultViewModel.getPendingProcessLiveData()
            .observe(viewLifecycleOwner, pendingProcessObserver)
        mResultViewModel.getProcessingLiveData().observe(viewLifecycleOwner, processingObserver)
        mResultViewModel.getProcessDoneLiveData().observe(viewLifecycleOwner, processDoneObserver)
        mResultViewModel.getPlayerInfo().observe(viewLifecycleOwner, playInfoObserver)
        mResultViewModel.getErrorLiveData().observe(viewLifecycleOwner, errorObserver)
        mResultViewModel.getCancelLiveData().observe(viewLifecycleOwner, cancelObserver)

        ManagerFactory.getAudioEditorManager().getLastItem()
            .observe(viewLifecycleOwner, lastItemObserver)
        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (contactPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and CONTACTS_ITEM_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    audioFile?.let {
                        viewStateManager.resultScreenSetContactItemClicked(this, it.file.absolutePath)
                    }
                }
                if (writeSettingPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and WRITESETTING_ITEM_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    when (numberClick) {
                        1 -> {
//                            setRingtone()
                            if (mResultViewModel.setRingTone()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_ringtone_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_ringtone_fail))
                                }
                            }
                        }
                        2 -> {
//                            setAlarm()
                            if (mResultViewModel.setAlarm()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_alarm_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_alarm_fail))
                                }
                            }
                        }
                        3 -> {
//                            setNotifiCation()
                            if (mResultViewModel.setNotification()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_notification_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_notification_fail))
                                }
                            }
                        }
                    }
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
        binding.pbLoading.max = 100 * 100

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
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (playerState != PlayerState.IDLE) {
                    if (fromUser) {
                        binding.sbMusic.clearAnimation()
                        sbAnimation?.cancel()
                        setSeekbarAnimate(binding.sbMusic, progress / 100, DURATION_ANIMATION)
                    }
                }
                if (playerState != PlayerState.PLAYING) {
                    binding.tvTimeLife.text = Utils.toTimeStr(progress.toLong() / 100, timeFomat)
                }

                if (progress == binding.sbMusic.max) {
                    Log.d(TAG, "onProgressChanged: progress == max ")
                    resetAnimation()
                    binding.sbMusic.progress = 0
                    playerState = PlayerState.IDLE
                    binding.ivPausePlayMusic.setImageResource(R.drawable.common_ic_play)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                if (playerState == PlayerState.PLAYING) {
                    mResultViewModel.pauseAudio()
                    resetAnimation()
                    isSeekBarStatus = true
                }
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                if (playerState == PlayerState.IDLE) {
                    lifecycleScope.launch {
                        resetAnimation()
//                        lifecycleScope.launch {
                        val audioFile = ManagerFactory.getAudioEditorManager()
                            .getLatestConvertingItem()?.outputAudioFile
                        audioFile?.let {
                            val newValue = Utils.convertValue(0, binding.sbMusic.max, 0, it.duration.toInt(), binding.sbMusic.progress)
                            mResultViewModel.playAudioByPositition(it, newValue)
                        }
                    }
//                    }
                }
                resetAnimation()
                mResultViewModel.seekToAudio(binding.sbMusic.progress / 100)
                mResultViewModel.resumeAudio()
                isSeekBarStatus = false
            }
        })
    }

    private fun resetAnimation() {
        binding.sbMusic.clearAnimation()
        sbAnimation?.cancel()
    }

    fun checkAudiofileIsExits(audioFile: AudioFile): Boolean {
        return Utils.checkUriIsExits(requireContext(), audioFile.uri.toString())
    }

    override fun onClick(view: View?) {
        audioFile = ManagerFactory.getAudioEditorManager()
            .getLatestConvertingItem()?.outputAudioFile
        when (view) {
            binding.ivPausePlayMusic -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        when (playerState) {
                            PlayerState.IDLE -> {
                                runOnUI {
                                    mResultViewModel.playAudio()
                                    resetAnimation()
                                    binding.sbMusic.progress = 0
                                    setSeekbarAnimate(binding.sbMusic, 0, DURATION_ANIMATION)
                                }
                            }
                            PlayerState.PAUSE -> {
                                mResultViewModel.resumeAudio()
                            }
                            PlayerState.PLAYING -> {
                                mResultViewModel.pauseAudio()
                            }
                            else -> {
                                //nothing
                            }
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
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
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        numberClick = 1
                        if (writeSettingPermissionRequest.isPermissionGranted()) {
//                            setRingtone()
                            if (mResultViewModel.setRingTone()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_ringtone_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_ringtone_fail))
                                }
                            }
                        } else {
                            resetRequestingPermission()
                            pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
                            writeSettingPermissionRequest.requestPermission()
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }

            }
            binding.llAlarm -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        numberClick = 2
                        if (writeSettingPermissionRequest.isPermissionGranted()) {
//                            setAlarm()
                            if (mResultViewModel.setAlarm()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_alarm_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_alarm_fail))
                                }
                            }
                        } else {
                            resetRequestingPermission()
                            pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
                            writeSettingPermissionRequest.requestPermission()
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }

            }

            binding.llNotification -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        numberClick = 3
                        if (writeSettingPermissionRequest.isPermissionGranted()) {
//                            setNotifiCation()
                            if (mResultViewModel.setNotification()) {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_notification_successful))
                                }
                            } else {
                                context?.let {
                                    showNotification(getString(R.string.result_screen_set_notification_fail))
                                }
                            }
                        } else {
                            resetRequestingPermission()
                            pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
                            writeSettingPermissionRequest.requestPermission()
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }

            }
            binding.llShare -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        ShowDialogShareFile()
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }
            }
            binding.llContact -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        checkPermissionContact()
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }
            }
            binding.llOpenwith -> {
                audioFile?.let {
                    if (checkAudiofileIsExits(it)) {
                        audioFile?.uri?.let {
                            Utils.openWithApp(requireContext(), it)
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.result_screen_file_not_is_exit))
                        }
                    }
                }

            }
        }
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

//    private fun setNotifiCation() {
//        if (mResultViewModel.setNotification()) {
//            generateToast(getString(R.string.result_screen_set_notification_successful))
//        } else {
//            generateToast(getString(R.string.result_screen_set_notification_fail))
//        }
//    }

//    @SuppressLint("ShowToast")
//    private fun generateToast(text: String) {
//        if (toast != null) {
//            toast!!.cancel()
//            toast = null
//            toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
//        } else if (toast == null) {
//            toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
//        }
//        toast!!.show()
//    }

//    private fun setAlarm() {
//        if (mResultViewModel.setAlarm()) {
//            generateToast(getString(R.string.result_screen_set_alarm_successful))
//        } else {
//            generateToast(getString(R.string.result_screen_set_alarm_fail))
//        }
//    }
//
//    private fun setRingtone() {
//        if (mResultViewModel.setRingTone()) {
//            generateToast(getString(R.string.result_screen_set_ringtone_successful))
//        } else {
//            generateToast(getString(R.string.result_screen_set_ringtone_fail))
//        }
//    }

    private fun checkPermissionContact() {
        if (contactPermissionRequest.isPermissionGranted()) {
            audioFile?.let {
                viewStateManager.resultScreenSetContactItemClicked(this, it.file.absolutePath)
            }
        } else {
            ContactPermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = CONTACTS_ITEM_REQUESTING_PERMISSION
                contactPermissionRequest.requestPermission()
            }
                .show(requireActivity().supportFragmentManager, ContactPermissionDialog::class.java.name)
        }
    }


    private fun ShowDialogShareFile() {
        dialogAppShare = DialogAppShare(requireContext(), Utils.getListAppQueryReceiveOnlyData(requireContext()), TypeShare.ONLYFILE)
        dialogAppShare.setOnCallBack(this)
        dialogAppShare.show(requireActivity().supportFragmentManager, "TAG_DIALOG")
    }

    override fun shareFileAudioToAppDevices(multifile: TypeShare) {
        dialogAppShare.dismiss()
        audioFile?.uri?.let {
            Utils.shareFileAudio(requireContext(), it, null)
        }
    }

    override fun shareFilesToAppsDialog(pkgName: String, typeShare: TypeShare, isDialogMulti: Boolean?) {
        if (typeShare == TypeShare.ONLYFILE) {
            audioFile?.uri?.let {
                Utils.shareFileAudio(requireContext(), it, pkgName)
            }
        }
    }


    override fun onCancelDeleteClick(id: Int) {             // interface delete dialog
        ManagerFactory.getAudioEditorManager().cancel(id)
        requireActivity().onBackPressed()
    }

    override fun onCancelDialog() {
        // no thing
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        toast?.let {
//            toast!!.cancel()
//        }
        progressbarAnimation?.cancel()
    }

    override fun onStop() {
        super.onStop()
        mResultViewModel.stopAudio()
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

    fun setProgressAnimate(pb: ProgressBar, progressTo: Int, duration: Long) {
        // smooth animation
        progressbarAnimation?.cancel()
        progressbarAnimation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
        progressbarAnimation?.duration = duration
        progressbarAnimation?.interpolator = DecelerateInterpolator()
        progressbarAnimation?.start()
    }
    init {
        writeSettingPermissionRequest.init()
        contactPermissionRequest.init()
    }
}
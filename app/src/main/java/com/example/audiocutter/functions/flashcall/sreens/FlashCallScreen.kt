package com.example.audiocutter.functions.flashcall.sreens

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.FlashCallConfig
import com.example.audiocutter.core.manager.FlashType
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.FlashCallScreenBinding
import com.example.audiocutter.functions.flashcall.dialogs.FlashTypeDialog
import com.example.audiocutter.functions.flashcall.dialogs.NotificationDialog
import com.example.audiocutter.functions.flashcall.dialogs.SettimeDialog
import com.example.audiocutter.functions.flashcall.dialogs.SuggestionDialog
import com.example.audiocutter.functions.mystudio.dialog.RenameDialog
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.NotificationListenerPermissionRequest
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.util.Utils


class FlashCallScreen : BaseFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener, SettimeDialog.SettimeListener, SeekBar.OnSeekBarChangeListener, FlashTypeDialog.FlashTypeListener, NotificationDialog.NotifiCationListener {
    private lateinit var dialogNotification: NotificationDialog
    private lateinit var flashTypeDialog: FlashTypeDialog
    private val TAG: String = "lll"
    private lateinit var dialogSettime: SettimeDialog
    private lateinit var binding: FlashCallScreenBinding
    private var numCheckClick = 0
    private lateinit var typeFlash: FlashType
    val manager = ManagerFactory.getFlashCallSetting()

    private lateinit var flashModel: FlashCallModel
    private lateinit var flashCallConfig: FlashCallConfig

    private val MAX_PROGRESS = 27
    private val MIN_PROGRESS = 0
    private val MAX_VALUE = 1500
    private val MIN_VALUE = 150
    private val NOTIFICATION_LISTENER_REQUESTING_PERMISSION = 1 shl 1
    private var pendingRequestingPermission = 0

    private var startHour = -1
    private var startMinute = -1


    @SuppressLint("SetTextI18n")
    var flashObserver = Observer<FlashCallConfig> {
//        Log.d(TAG, "checkEnable:${it.enable}  \n incomming ${it.incomingCallEnable}  \n notifi ${it.notificationEnable}   \nfiredWhenInUsed ${it.notFiredWhenInUsed}  ")
        flashCallConfig = it
        showOrHideFlashMode(it.enable)

        binding.swFlashCallMode.isChecked = it.enable
        binding.swIncommingCall.isChecked = it.incomingCallEnable
        binding.swNotifycation.isChecked = it.notificationEnable
        Log.d(TAG, "checkNotifObserve: ${it.notificationEnable}")
        binding.tbAppFlashcall.isEnabled = it.notificationEnable
        if (it.notificationEnable) {
            binding.tvNotification.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
        } else {
            binding.tvNotification.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
        }
        binding.swInUse.isChecked = it.notFiredWhenInUsed
        binding.swBellFlashcall.isChecked = it.flashMode.bellEnable
        binding.swVibrateFlashcall.isChecked = it.flashMode.vibrateEnable
        binding.swSilentFlashCall.isChecked = it.flashMode.silentEnable
        binding.swSettimeFlash.isChecked = it.flashTimer.enable
        binding.sbNumberOfLightning.progress = it.numberOfLightning
        binding.sbLinghtningSpeedFlcall.progress = Utils.convertValue(MIN_VALUE, MAX_VALUE, MIN_PROGRESS, MAX_PROGRESS, it.lightningSpeed.toInt())

        binding.tvNumberOfLightning.text = " ${it.numberOfLightning} times"
        binding.tvLightningSpeedFlcall.text = " ${it.lightningSpeed} ms"


        val startHour = checkValidTimes(it.flashTimer.startHour)
        val startMinute = checkValidTimes(it.flashTimer.startMinute)
        val endHour = checkValidTimes(it.flashTimer.endHour)
        val endMinute = checkValidTimes(it.flashTimer.endMinute)

        Log.d(TAG, "checkTimedefault: ${it.flashTimer.startHour} : ${it.flashTimer.startMinute}   -- ${it.flashTimer.endHour}:${it.flashTimer.endMinute}")


        onOffSetTimeFlash(it.flashTimer.enable)
        if ((it.flashTimer.startHour and it.flashTimer.startMinute and it.flashTimer.endHour and it.flashTimer.endMinute) != -1) {
            binding.tvStartTimeChoose.text = "${startHour}:${startMinute}"
            binding.tvEndTimeChoose.text = "${endHour}:${endMinute}"
        }

        if (!flashCallConfig.isLightingSpeedDefault()) {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_undefault_flcall)
            binding.tvDefaultSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
        } else {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_default_flcall)
            binding.tvDefaultSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        }
        typeFlash = it.flashType
        when (it.flashType) {
            FlashType.BEAT -> {
                binding.tvTypeflashFlcall.text = getString(R.string.beat_type_flash_call)
            }
            FlashType.CONTINUITY -> {
                binding.tvTypeflashFlcall.text = getString(R.string.Continuity_flash_call)
            }
        }

    }

    private fun checkValidTimes(time: Int): String {
        return "%02d".format(time)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        flashModel = ViewModelProvider(this).get(FlashCallModel::class.java)
        flashModel.getFlashCallConfig().observe(this, flashObserver)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.flash_call_screen, container, false)
        val result = Utils.checkFlashOnDeviceAvailable(requireContext())
        result?.let {
            if (result) {
                showItemDeviceFlashAvailable()
            } else {
                showItemDeviceFlashUnAvailable()
            }
        }
        initViews()

        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (notificationListenerPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and NOTIFICATION_LISTENER_REQUESTING_PERMISSION) != 0) {
                    Log.d(TAG, "onCreateView: permission")
                    resetRequestingPermission()
                    binding.swNotifycation.isChecked = true
                }
            })


        return binding.root
    }

    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }

    private fun initViews() {
        Log.d(TAG, "checkFunc is the program running:  start ")
        binding.ivFlashCallScreenBack.setOnClickListener(this)
        binding.tbStartTime.isEnabled = false
        binding.tbEndTime.isEnabled = false
        binding.ivSuggestion.setOnClickListener(this)
        /**tablerow**/
        binding.tbAppFlashcall.setOnClickListener(this)
        binding.tbFlashType.setOnClickListener(this)
        binding.tbStartTime.setOnClickListener(this)
        binding.tbEndTime.setOnClickListener(this)

        binding.tvTestSpeedFlashcall.setOnClickListener(this)
        binding.tvStopTestSpeedFlashcall.setOnClickListener(this)
        /**switchcompat**/
        binding.swFlashCallMode.setOnCheckedChangeListener(this)
        binding.swSettimeFlash.setOnCheckedChangeListener(this)
        binding.swIncommingCall.setOnCheckedChangeListener(this)
        binding.swNotifycation.setOnCheckedChangeListener(this)
        binding.swInUse.setOnCheckedChangeListener(this)
        binding.swBellFlashcall.setOnCheckedChangeListener(this)
        binding.swVibrateFlashcall.setOnCheckedChangeListener(this)
        binding.swSilentFlashCall.setOnCheckedChangeListener(this)


        /**seekbar**/
        binding.sbNumberOfLightning.setOnSeekBarChangeListener(this)
        binding.sbLinghtningSpeedFlcall.setOnSeekBarChangeListener(this)

        // default speed
        binding.tvDefaultSpeed.setOnClickListener(this)
    }

    private fun showItemDeviceFlashUnAvailable() {
        binding.ivFlashUnavailable.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_unavalable))
        binding.tvFlashUnavailable.text = getString(R.string.flash_ondevice_unavailable)
        binding.lnFlashOffMode.visibility = View.VISIBLE
        binding.scrModeOnflash.visibility = View.INVISIBLE
        binding.swFlashCallMode.visibility = View.INVISIBLE
    }

    private fun showItemDeviceFlashAvailable() {
        binding.lnFlashOffMode.visibility = View.INVISIBLE
        binding.scrModeOnflash.visibility = View.VISIBLE
        binding.swFlashCallMode.visibility = View.VISIBLE
    }

    @SuppressLint("ResourceAsColor")
    override fun onCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        when (view) {
            binding.swFlashCallMode -> {
                flashCallConfig.enable = isChecked
                showOrHideFlashMode(isChecked)
            }

            binding.swSettimeFlash -> {
                flashCallConfig.flashTimer.enable = isChecked
            }
            binding.swIncommingCall -> {
                flashCallConfig.incomingCallEnable = isChecked
            }
            binding.swNotifycation -> {
                if (isChecked) {
                    if (notificationListenerPermissionRequest.isPermissionGranted()) {
                        flashCallConfig.notificationEnable = isChecked
                    } else {
                        dialogNotification = NotificationDialog()
                        dialogNotification.setOnCallBack(this)
                        dialogNotification.show(childFragmentManager, NotificationDialog::class.java.name)
                    }
//                    pendingRequestingPermission = NOTIFICATION_LISTENER_REQUESTING_PERMISSION
//                    notificationListenerPermissionRequest.requestPermission()
                } else {
                    flashCallConfig.notificationEnable = isChecked
                    Log.d(TAG, "onCheckedChanged: notification enable  $isChecked")
                }

            }
            binding.swInUse -> {
                flashCallConfig.notFiredWhenInUsed = isChecked
            }
            binding.swBellFlashcall -> {
                flashCallConfig.flashMode.bellEnable = isChecked
            }
            binding.swVibrateFlashcall -> {
                flashCallConfig.flashMode.vibrateEnable = isChecked
            }
            binding.swSilentFlashCall -> {
                flashCallConfig.flashMode.silentEnable = isChecked
            }
        }
        changeFlashConfig(flashCallConfig)
        Build.VERSION_CODES.M

    }


    private fun onOffSetTimeFlash(isChecked: Boolean) {
        if (isChecked) {
            binding.tbStartTime.isEnabled = true
            binding.tbEndTime.isEnabled = true
            binding.tvStartTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            binding.tvEndTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
        } else {
            binding.tbStartTime.isEnabled = false
            binding.tbEndTime.isEnabled = false
            binding.tvStartTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
            binding.tvEndTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
        }
    }

    private fun showOrHideFlashMode(isChecked: Boolean) {
        if (isChecked) {
            binding.lnFlashOffMode.visibility = View.INVISIBLE
            binding.lnFlashOnMode.visibility = View.VISIBLE
        } else {
            binding.lnFlashOffMode.visibility = View.VISIBLE
            binding.lnFlashOnMode.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View) {
        dialogSettime = SettimeDialog()
        dialogSettime.setOnCallBack(this)
        when (v) {
            binding.tbAppFlashcall -> {
                viewStateManager.flashCallOnNotificationForAppsItemClicked(this)
            }
            binding.ivFlashCallScreenBack -> {
                requireActivity().onBackPressed()
            }
            binding.tbFlashType -> {
                ManagerFactory.getFlashCallSetting().stopTestingLightningSpeed()
                changeColorButton(R.color.colorgray, R.color.colorYelowDark)
                val num: Int = when (typeFlash) {
                    FlashType.CONTINUITY -> 1
                    FlashType.BEAT -> 0
                }
                flashTypeDialog = FlashTypeDialog.newInstance(num)
                flashTypeDialog.setOnCallBack(this)
                flashTypeDialog.show(childFragmentManager, FlashTypeDialog::class.java.name)
                numCheckClick = 0
            }
            binding.tbEndTime -> {
                numCheckClick = 2
                if (startHour == -1 || startMinute == -1) {
                    startHour = flashCallConfig.flashTimer.startHour
                    startMinute = flashCallConfig.flashTimer.startMinute
                }
                val time = startHour * 60 + startMinute
                val dialog = SettimeDialog.newInstance(this, time, startMinute, numCheckClick)
                dialogSettime = dialog
                dialog.show(childFragmentManager, SettimeDialog::class.java.name)

            }
            binding.tbStartTime -> {
                numCheckClick = 1
                val dialog = SettimeDialog.newInstance(this, startHour, startMinute, numCheckClick)
                dialogSettime = dialog
                dialog.show(childFragmentManager, SettimeDialog::class.java.name)
            }
            binding.tvTestSpeedFlashcall -> {
                numCheckClick = 3
                ManagerFactory.getFlashCallSetting().startTestingLightningSpeed()
                changeColorButton(R.color.colorYelowDark, R.color.colorgray)
            }
            binding.tvStopTestSpeedFlashcall -> {
                numCheckClick = 4
                ManagerFactory.getFlashCallSetting().stopTestingLightningSpeed()
                changeColorButton(R.color.colorgray, R.color.colorYelowDark)
            }
            binding.ivSuggestion -> {
                val dialog = SuggestionDialog()
                dialog.show(childFragmentManager, SuggestionDialog::class.java.name)
            }

            binding.tvDefaultSpeed -> {
                val speedValue = Utils.convertValue(MIN_PROGRESS, MAX_PROGRESS, MIN_VALUE, MAX_VALUE, 4)
                flashCallConfig.lightningSpeed = speedValue.toLong()
                if (numCheckClick == 3) {
                    ManagerFactory.getFlashCallSetting().startTestingLightningSpeed()
                }
                changeFlashConfig(flashCallConfig)
            }
        }
    }

    private fun changeColorButton(color1: Int, color2: Int) {
        binding.tvTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), color1))
        binding.tvStopTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), color2))
    }

    interface FlashCallBack {
        /**test , when handle then delete**/
        fun showFrgs()
    }

    @SuppressLint("SetTextI18n")
    override fun changeTimeFlash(hours: Int, minute: Int) {
        when (numCheckClick) {
            1 -> {
                flashCallConfig.flashTimer.startHour = hours
                flashCallConfig.flashTimer.startMinute = minute

                flashCallConfig.flashTimer.endHour = hours
                flashCallConfig.flashTimer.endMinute = minute

                startHour = hours
                startMinute = minute

                dialogSettime.dismiss()
            }
            2 -> {
                flashCallConfig.flashTimer.endHour = hours
                flashCallConfig.flashTimer.endMinute = minute
                dialogSettime.dismiss()
            }
        }
        changeFlashConfig(flashCallConfig)
    }

    private fun changeFlashConfig(flashCallConfig: FlashCallConfig) {
        ManagerFactory.getFlashCallSetting().changeFlashCallConfig(flashCallConfig)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        Log.d(TAG, "nmcode: $progress")
        when (seekBar) {
            binding.sbNumberOfLightning -> {
                val value: Int = if (progress < 1) {
                    1
                } else {
                    progress
                }
                flashCallConfig.numberOfLightning = value
            }
            binding.sbLinghtningSpeedFlcall -> {

                val speedValue = Utils.convertValue(MIN_PROGRESS, MAX_PROGRESS, MIN_VALUE, MAX_VALUE, progress)
                flashCallConfig.lightningSpeed = speedValue.toLong()
                if (fromUser && numCheckClick == 3) {
                    ManagerFactory.getFlashCallSetting().startTestingLightningSpeed()
                }
            }
        }
        changeFlashConfig(flashCallConfig)

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun changeMode(type: FlashType) {
        flashTypeDialog.dismiss()
        when (type) {
            FlashType.CONTINUITY -> {
                flashCallConfig.flashType = FlashType.CONTINUITY
                binding.tvTypeflashFlcall.text = getString(R.string.Continuity_flash_call)
            }
            FlashType.BEAT -> {
                flashCallConfig.flashType = FlashType.BEAT
                binding.tvTypeflashFlcall.text = getString(R.string.beat_type_flash_call)
            }
        }
        changeFlashConfig(flashCallConfig)
    }

    private val notificationListenerPermissionRequest = object : NotificationListenerPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    init {
        notificationListenerPermissionRequest.init()
    }

    override fun allowNotificationPermission() {
        dialogNotification.dismiss()
        pendingRequestingPermission = NOTIFICATION_LISTENER_REQUESTING_PERMISSION
        notificationListenerPermissionRequest.requestPermission()
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        ManagerFactory.getFlashCallSetting().stopTestingLightningSpeed()
    }

}

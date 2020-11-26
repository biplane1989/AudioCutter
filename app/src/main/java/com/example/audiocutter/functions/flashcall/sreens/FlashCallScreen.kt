package com.example.audiocutter.functions.flashcall.sreens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.FlashCallConfig
import com.example.audiocutter.core.manager.FlashType
import com.example.audiocutter.core.manager.LIGHTING_SPEED_DEFAULT
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.FlashCallScreenBinding
import com.example.audiocutter.functions.flashcall.dialogs.FlashTypeDialog
import com.example.audiocutter.functions.flashcall.dialogs.SettimeDialog
import com.example.audiocutter.functions.flashcall.dialogs.TypeFlash
import com.example.audiocutter.util.Utils

class FlashCallScreen : BaseFragment(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener, SettimeDialog.SettimeListener, SeekBar.OnSeekBarChangeListener,
    FlashTypeDialog.FlashTypeListener {
    private lateinit var flashTypeDialog: FlashTypeDialog
    private val TAG: String = "lll"
    private lateinit var dialogSettime: SettimeDialog
    private lateinit var binding: FlashCallScreenBinding
    private var numCheckClick = 0
    val manager = ManagerFactory.getFlashCallSetting()

    private lateinit var flashModel: FlashCallModel
    private lateinit var flashCallConfig: FlashCallConfig

    private val MAX_PROGRESS = 27
    private val MIN_PROGRESS = 0
    private val MAX_VALUE = 1500
    private val MIN_VALUE = 150

    @SuppressLint("SetTextI18n")
    var flashObserver = Observer<FlashCallConfig> {
//        Log.d(TAG, "checkEnable:${it.enable}  \n incomming ${it.incomingCallEnable}  \n notifi ${it.notificationEnable}   \nfiredWhenInUsed ${it.notFiredWhenInUsed}  ")
        flashCallConfig = it
        showOrHideFlashMode(it.enable)

        binding.swFlashCallMode.isChecked = it.enable
        binding.swIncommingCall.isChecked = it.incomingCallEnable
        binding.swNotifycation.isChecked = it.notificationEnable
        binding.tbAppFlashcall.isEnabled = it.notificationEnable
        if (it.notificationEnable) {
            binding.tvNotification.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
        } else {
            binding.tvNotification.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorgray
                )
            )
        }
        binding.swInUse.isChecked = it.notFiredWhenInUsed
        binding.swBellFlashcall.isChecked = it.flashMode.bellEnable
        binding.swVibrateFlashcall.isChecked = it.flashMode.vibrateEnable
        binding.swSilentFlashCall.isChecked = it.flashMode.silentEnable
        binding.swSettimeFlash.isChecked = it.flashTimer.enable

        binding.sbNumberOfLightning.progress = it.numberOfLightning

        /**bug*/
        Log.d(
            TAG,
            "onProgressChanged111:  ${Utils.convertValue(
                MIN_VALUE,
                MAX_VALUE,
                MIN_PROGRESS,
                MAX_PROGRESS,
                it.lightningSpeed.toInt()
            )}"
        )
        binding.sbLinghtningSpeedFlcall.progress = Utils.convertValue(
            MIN_VALUE,
            MAX_VALUE,
            MIN_PROGRESS,
            MAX_PROGRESS,
            it.lightningSpeed.toInt()
        )

        binding.tvNumberOfLightning.text = " ${it.numberOfLightning} times"
        binding.tvLightningSpeedFlcall.text = " ${it.lightningSpeed} ms"


        val startHour = checkValidTimes(it.flashTimer.startHour)
        val startMinute = checkValidTimes(it.flashTimer.startMinute)
        val endHour = checkValidTimes(it.flashTimer.endHour)
        val endMinute = checkValidTimes(it.flashTimer.endMinute)




        onOffSetTimeFlash(it.flashTimer.enable)
        if ((it.flashTimer.startHour and it.flashTimer.startMinute and it.flashTimer.endHour and it.flashTimer.endMinute) != -1) {
            binding.tvStartTimeChoose.text = "${startHour}:${startMinute}"
            binding.tvEndTimeChoose.text = "${endHour}:${endMinute}"
        }

        if (it.lightningSpeed != LIGHTING_SPEED_DEFAULT) {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_undefault_flcall)
            binding.tvDefaultSpeed.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
        } else {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_default_flcall)
            binding.tvDefaultSpeed.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
        }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.flash_call_screen, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.tbStartTime.isEnabled = false
        binding.tbEndTime.isEnabled = false
        /**tablerow**/
        binding.tbAppFlashcall.setOnClickListener(this)
        binding.tbFlashType.setOnClickListener(this)
        binding.tbStartTime.setOnClickListener(this)
        binding.tbEndTime.setOnClickListener(this)

        binding.tvTestSpeedFlashcall.setOnClickListener(this)
        binding.tvStopTestSpeedFlashcall.setOnClickListener(this)
        binding.ivCutterScreenBack.setOnClickListener(this)
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

                flashCallConfig.notificationEnable = isChecked
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

    }


    private fun onOffSetTimeFlash(isChecked: Boolean) {
        if (isChecked) {
            binding.tbStartTime.isEnabled = true
            binding.tbEndTime.isEnabled = true
            binding.tvStartTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
            binding.tvEndTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
        } else {
            binding.tbStartTime.isEnabled = false
            binding.tbEndTime.isEnabled = false
            binding.tvStartTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorgray
                )
            )
            binding.tvEndTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorgray
                )
            )
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
            binding.ivCutterScreenBack -> {
                requireActivity().onBackPressed()
            }
            binding.tbFlashType -> {
                flashTypeDialog = FlashTypeDialog()
                flashTypeDialog.setOnCallBack(this)
                flashTypeDialog.show(childFragmentManager, "TAG")
            }
            binding.tbEndTime -> {
                numCheckClick = 2
                dialogSettime.show(childFragmentManager, "TAG")
            }
            binding.tbStartTime -> {
                numCheckClick = 1
                dialogSettime.show(childFragmentManager, "TAG")
            }
            binding.tvTestSpeedFlashcall -> {
                ManagerFactory.getFlashCallSetting().startTestingLightningSpeed()
                binding.tvTestSpeedFlashcall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorYelowDark
                    )
                )
                binding.tvStopTestSpeedFlashcall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorgray
                    )
                )
            }
            binding.tvStopTestSpeedFlashcall -> {
                ManagerFactory.getFlashCallSetting().stopTestingLightningSpeed()
                binding.tvTestSpeedFlashcall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorgray
                    )
                )
                binding.tvStopTestSpeedFlashcall.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorYelowDark
                    )
                )
            }

        }
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

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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

                val speedValue =
                    Utils.convertValue(MIN_PROGRESS, MAX_PROGRESS, MIN_VALUE, MAX_VALUE, progress)

                flashCallConfig.lightningSpeed = speedValue.toLong()
                Log.d(TAG, "onProgressChanged:  ${flashCallConfig.lightningSpeed}")
            }
        }
        changeFlashConfig(flashCallConfig)

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun changeMode(type: TypeFlash) {
        flashTypeDialog.dismiss()
        when (type) {
            TypeFlash.CONTINUITY -> {
                flashCallConfig.flashType = FlashType.CONTINUITY
                binding.tvTypeflashFlcall.text = getString(R.string.Continuity_flash_call)
            }
            TypeFlash.BEAT -> {
                flashCallConfig.flashType = FlashType.BEAT
                binding.tvTypeflashFlcall.text = getString(R.string.beat_type_flash_call)
            }
        }
        changeFlashConfig(flashCallConfig)
    }

}

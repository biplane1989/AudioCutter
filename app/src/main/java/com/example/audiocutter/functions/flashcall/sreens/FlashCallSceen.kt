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
import com.example.audiocutter.core.manager.LIGHTING_SPEED_DEFAULT
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.FlashCallScreenBinding
import com.example.audiocutter.functions.flashcall.dialogs.FlashTypeDialog
import com.example.audiocutter.functions.flashcall.dialogs.SettimeDialog
import com.example.audiocutter.functions.flashcall.dialogs.TypeFlash
import com.example.audiocutter.util.Utils

class FlashCallSceen : BaseFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener, SettimeDialog.SettimeListener, SeekBar.OnSeekBarChangeListener, FlashTypeDialog.FlashTypeListener {
    private lateinit var flashTypeDialog: FlashTypeDialog
    private val TAG: String = "lll"
    private lateinit var dialogSettime: SettimeDialog
    private lateinit var binding: FlashCallScreenBinding
    private lateinit var mCallBack: FlashCallBack
    private var numCheckClick = 0
    val manager = ManagerFactory.getFlashCallSetting()

    private lateinit var flashModel: FlashCallModel
    private lateinit var flashCallConfig: FlashCallConfig


    @SuppressLint("SetTextI18n")
    var flashObserver = Observer<FlashCallConfig> {
//        Log.d(TAG, "checkEnable:${it.enable}  \n incomming ${it.incomingCallEnable}  \n notifi ${it.notificationEnable}   \nfiredWhenInUsed ${it.notFiredWhenInUsed}  ")
        flashCallConfig = it
        showOrHideFlashMode(it.enable)

        binding.swFlashCallMode.isChecked = it.enable
        binding.swIncommingCall.isChecked = it.incomingCallEnable
        binding.swNotifycation.isChecked = it.notificationEnable
        binding.swInUse.isChecked = it.notFiredWhenInUsed
        binding.swBellFlashcall.isChecked = it.flashMode.bellEnable
        binding.swVibrateFlashcall.isChecked = it.flashMode.vibrateEnable
        binding.swSilentFlashCall.isChecked = it.flashMode.silentEnable
        binding.swSettimeFlash.isChecked = it.flashTimer.enable

        binding.sbNumberOfLightning.progress = it.numberOfLightning

        /**bug*/
        Log.d(TAG, "onProgressChanged111:  ${Utils.convertValue(150, 1500, 0, 50, it.lightningSpeed.toInt())}")
        binding.sbLinghtningSpeedFlcall.progress = Utils.convertValue(150, 1500, 0, 50, it.lightningSpeed.toInt())

        binding.tvNumberOfLightning.text = " ${it.numberOfLightning} times"
        binding.tvLightningSpeedFlcall.text = " ${it.lightningSpeed} ms"

        val startHour = it.flashTimer.startHour
        val startMinute = it.flashTimer.startMinute
        val endHour = it.flashTimer.endHour
        val endMinute = it.flashTimer.endMinute

        onOffSetTimeFlash(it.flashTimer.enable)
        if ((startHour and startMinute and endHour and endMinute) != -1) {
            binding.tvStartTimeChoose.text = "${it.flashTimer.startHour}:${it.flashTimer.startMinute}"
            binding.tvEndTimeChoose.text = "${it.flashTimer.endHour}:${it.flashTimer.endMinute}"
        }

        if (it.lightningSpeed != LIGHTING_SPEED_DEFAULT) {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_undefault_flcall)
            binding.tvDefaultSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
        } else {
            binding.tvDefaultSpeed.setBackgroundResource(R.drawable.bg_default_flcall)
            binding.tvDefaultSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        }


    }

    fun setOnCallBack(event: FlashCallBack) {
        mCallBack = event
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        flashModel = ViewModelProvider(this).get(FlashCallModel::class.java)
        flashModel.getFlashCallConfig().observe(this, flashObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
                mCallBack.showFrgs()
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
                binding.tvTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorYelowDark))
                binding.tvStopTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
            }
            binding.tvStopTestSpeedFlashcall -> {
                ManagerFactory.getFlashCallSetting().stopTestingLightningSpeed()
                binding.tvTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorgray))
                binding.tvStopTestSpeedFlashcall.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorYelowDark))
            }

        }
    }

    interface FlashCallBack {
        /**test , when handle then delete**/
        fun showFrgs()
    }

    @SuppressLint("SetTextI18n")
    override fun changeTimeFlash(hours: Int, minute: Int) {
        val textMinute = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val textHours = if (hours < 10) {
            "0$hours"
        } else {
            "$hours"
        }
        when (numCheckClick) {
            1 -> {
                flashCallConfig.flashTimer.startHour = hours
                flashCallConfig.flashTimer.startMinute = minute

                dialogSettime.dismiss()
                binding.tvStartTimeChoose.text = "$textHours:$textMinute"
            }
            2 -> {
                flashCallConfig.flashTimer.endHour = hours
                flashCallConfig.flashTimer.endMinute = minute
                dialogSettime.dismiss()
                binding.tvEndTimeChoose.text = "$textHours:$textMinute"
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
                binding.sbNumberOfLightning.progress = value
                binding.tvNumberOfLightning.text = "$value times"
                flashCallConfig.numberOfLightning = value
            }

            binding.sbLinghtningSpeedFlcall -> {

                val speedValue = Utils.convertValue(0, 50, 150, 1500, progress)
                binding.tvLightningSpeedFlcall.text = "${speedValue} ms"
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
                binding.tvTypeflashFlcall.text = getString(R.string.Continuity_flash_call)
            }
            TypeFlash.BEAT -> {
                binding.tvTypeflashFlcall.text = getString(R.string.beat_type_flash_call)
            }
        }
    }

}

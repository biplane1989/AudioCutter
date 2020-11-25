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
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FlashCallScreenBinding
import com.example.audiocutter.functions.flashcall.dialogs.FlashTypeDialog
import com.example.audiocutter.functions.flashcall.dialogs.SettimeDialog
import com.example.audiocutter.functions.flashcall.dialogs.TypeFlash

class FlashCallSceen : BaseFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener, SettimeDialog.SettimeListener, SeekBar.OnSeekBarChangeListener, FlashTypeDialog.FlashTypeListener {
    private lateinit var flashTypeDialog: FlashTypeDialog
    private val TAG: String = "lll"
    private lateinit var dialogSettime: SettimeDialog
    private lateinit var binding: FlashCallScreenBinding
    private lateinit var mCallBack: FlashCallBack
    private var numCheckClick = 0

    fun setOnCallBack(event: FlashCallBack) {
        mCallBack = event
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.flash_call_screen, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.tbStartTime.isEnabled = false
        binding.tbEndTime.isEnabled = false

        binding.tbAppFlashcall.setOnClickListener(this)
        binding.tbFlashType.setOnClickListener(this)
        binding.tbStartTime.setOnClickListener(this)
        binding.tbEndTime.setOnClickListener(this)
        binding.tvTestSpeedFlashcall.setOnClickListener(this)
        binding.tvStopTestSpeedFlashcall.setOnClickListener(this)

        binding.swFlashCallMode.setOnCheckedChangeListener(this)
        binding.swSettimeFlash.setOnCheckedChangeListener(this)
        binding.sbCountFlashes.setOnSeekBarChangeListener(this)

        binding.sbLinghtningSpeedFlcall.setOnSeekBarChangeListener(this)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        when (view) {
            binding.swFlashCallMode -> {
                showOrHideFlashMode(isChecked)
            }
            binding.swSettimeFlash -> {
                onOffSetTimeFlash(isChecked)
            }
        }
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
                dialogSettime.dismiss()
                binding.tvStartTimeChoose.text = "$textHours:$textMinute"
            }
            2 -> {
                dialogSettime.dismiss()
                binding.tvEndTimeChoose.text = "$textHours:$textMinute"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar) {
            binding.sbCountFlashes -> {
                if (progress < 1) {
                    binding.sbCountFlashes.progress = 1
                    binding.tvCountTimes.text = "1 times"
                } else {
                    binding.tvCountTimes.text = "$progress times"
                }
            }
            binding.sbLinghtningSpeedFlcall -> {
                if (progress < binding.sbLinghtningSpeedFlcall.max) {

                    val value = (binding.sbLinghtningSpeedFlcall.max - progress) * 50
                    binding.tvDurationFlcall.text = "$value ms"
                    Log.d(TAG, "onProgressChanged: $value")
                }
            }
        }
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

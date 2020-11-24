package com.example.audiocutter.functions.flashcall.sreens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.FlashCallScreenBinding
import com.example.audiocutter.functions.flashcall.dialogs.FlashTypeDialog
import com.example.audiocutter.functions.flashcall.dialogs.SettimeDialog

class FlashCallSceen : BaseFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener, SettimeDialog.SettimeListener {
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
        binding.swFlashCallMode.setOnCheckedChangeListener(this)
        binding.swSettimeFlash.setOnCheckedChangeListener(this)
        binding.tbAppFlashcall.setOnClickListener(this)
        binding.tbFlashType.setOnClickListener(this)
        binding.tbStartTime.setOnClickListener(this)
        binding.tbEndTime.setOnClickListener(this)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        when (view) {
            binding.swFlashCallMode -> {
                if (isChecked) {
                    binding.lnFlashOffMode.visibility = View.INVISIBLE
                    binding.lnFlashOnMode.visibility = View.VISIBLE
                } else {
                    binding.lnFlashOffMode.visibility = View.VISIBLE
                    binding.lnFlashOnMode.visibility = View.INVISIBLE
                }
            }
            binding.swSettimeFlash -> {

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
                val dialog = FlashTypeDialog()
                dialog.show(childFragmentManager, "TAG")
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

    override fun prevFrg() {
        when (numCheckClick) {
            1 -> {
                dialogSettime.dismiss()
                showToast("start time")
            }
            2 -> {
                dialogSettime.dismiss()
                showToast("end time")
            }
        }
    }

}

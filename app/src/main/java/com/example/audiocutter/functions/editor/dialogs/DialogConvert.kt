package com.example.audiocutter.functions.editor.dialogs

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.example.audiocutter.R
import com.example.audiocutter.activities.acttest.CoreTestActivity
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.editor.cutting.spinner.MaterialSpinner
import com.example.audiocutter.util.PreferencesHelper
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioCutConfig
import com.example.core.core.AudioFormat
import com.example.core.core.BitRate
import com.example.core.core.Effect
import com.google.android.material.slider.Slider
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DialogConvert : BaseDialog(), View.OnClickListener,
    Slider.OnChangeListener {
    private var listener: OnDialogConvertListener? = null

    private lateinit var audioFile: AudioFile
    private lateinit var edtNameFile: EditText
    private var volume = 0
    private lateinit var spinnerFormat: MaterialSpinner
    private lateinit var spinnerBitrate: MaterialSpinner
    private lateinit var seekBarVolume: Slider
    private lateinit var tvPercent: TextView
    private lateinit var tvCancel: TextView
    private lateinit var tvConvert: TextView
    private var listBitrate = ArrayList<String>()
    private var listFormat = ArrayList<String>()
    private var positionFormat = 0
    private var positionBitrate = 0

    override fun getLayoutResId(): Int {
        return R.layout.dialog_convert
    }

    companion object {
        fun showDialogConvert(
            fragmentManager: FragmentManager,
            listener: OnDialogConvertListener,
            audioFile: AudioFile
        ) {
            val bundle = Bundle()
            bundle.putString(Utils.KEY_SEND_AUDIO, audioFile.file.absolutePath)
            val dialogConvert =
                DialogConvert()
            dialogConvert.arguments = bundle
            dialogConvert.audioFile = audioFile
            dialogConvert.listener = listener
            dialogConvert.show(fragmentManager, "dialog convert")
        }
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        super.initViews(view, savedInstanceState)
        getData()
        edtNameFile = view.findViewById(R.id.name_edt)
        spinnerBitrate = view.findViewById(R.id.spinner_bitrate)
        spinnerFormat = view.findViewById(R.id.spinner_format)
        seekBarVolume = view.findViewById(R.id.seekbar)
        tvPercent = view.findViewById(R.id.percent_tv)
        tvPercent = view.findViewById(R.id.percent_tv)
        tvCancel = view.findViewById(R.id.dialog_convert_cancel_tv)
        tvConvert = view.findViewById(R.id.dialog_convert_ok_tv)

        seekBarVolume.value = volume.toFloat()
        edtNameFile.setText("name fake")
        tvPercent.text = String.format(Locale.ENGLISH, "%d%%", volume)

        spinnerFormat.apply {
            setItems(listFormat)
            selectedIndex = positionFormat
            setOnItemSelectedListener { view, position, id, item ->
                positionFormat = position
            }
        }
        spinnerBitrate.apply {
            setItems(listBitrate)
            selectedIndex = positionBitrate
            setOnItemSelectedListener { view, position, id, item -> positionBitrate = position }
        }
    }

    private fun getFileName(nameFile: String): String {
        val timeNow = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat(" hh:mm:ss dd/MM/yyyy", Locale.getDefault())
        val date = Date(timeNow)
        return nameFile.plus(" ${simpleDateFormat.format(date)}")
    }

    private fun getData() {
        audioFile = ManagerFactory.getAudioFileManager().buildAudioFile(requireArguments().getString(Utils.KEY_SEND_AUDIO)!!)
        AudioFormat.values().forEach { listFormat.add(it.name) }
        BitRate.values().forEach { listBitrate.add(it.name.replaceFirstCharacter()) }
        positionFormat =
            if (PreferencesHelper.getBoolean(PreferencesHelper.CONVERT_FORMAT, true)) 0 else 1
        positionBitrate = listBitrate.getItemPos(audioFile.bitRate)
        volume = PreferencesHelper.getInt(PreferencesHelper.CONVERT_VOLUME, 100)
    }

    override fun bindEvents() {
        super.bindEvents()
        tvCancel.setOnClickListener(this)
        tvConvert.setOnClickListener(this)
        seekBarVolume.addOnChangeListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dialog_convert_ok_tv -> {
                if (listener != null) {
                    listener!!.onAcceptConvert(
                        audioFile,
                        AudioCutConfig(
                            0F,
                            0F,
                            volume,
                            edtNameFile.text.toString().trim(),
                            Effect.OFF,
                            Effect.OFF,
                            BitRate.values()[positionBitrate],
                            AudioFormat.values()[positionFormat],
                            CoreTestActivity.PATH_CUT_FOLDER
                        )
                    )
                    addDataSharePre()
                }
            }
        }
        dismiss()
    }

    private fun addDataSharePre() {

        PreferencesHelper.putBoolean(
            PreferencesHelper.CONVERT_FORMAT,
            positionFormat == 0
        )
        PreferencesHelper.putInt(PreferencesHelper.CONVERT_VOLUME, volume)
    }

    interface OnDialogConvertListener {
        fun onAcceptConvert(audioFile: AudioFile, audioCutConfig: AudioCutConfig)
    }


    private fun String.replaceFirstCharacter(): String {
        return this.substring(1, this.length)
    }

    private fun ArrayList<String>.getItemPos(bitRate: Int): Int {
        for (i in 0 until this.size) {
            if (this[i].startsWith(bitRate.toString())) {
                return i
            }
        }
        return 0
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        volume = value.toInt()
        tvPercent.text = String.format(Locale.ENGLISH, "%d%%", volume)
    }
}
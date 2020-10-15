package com.example.audiocutter.functions.resultscreen

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.databinding.ResultScreenBinding
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.list_contact_screen.*
import java.io.File

class ResultScreen : BaseFragment() {

    val TAG = "giangtd"
    private lateinit var binding: ResultScreenBinding
    var audioId = 0
    lateinit var audioStatus: ConvertingState

    val processObserver = Observer<ConvertingItem> { it ->
        binding.pbResult.progress = it.percent
        audioId = it.id
        audioStatus = it.state
    }

    val listAudioObserver = Observer<List<ConvertingItem>> { it ->
        Log.d(TAG, "audio size : " + it.size)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.result_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(viewLifecycleOwner, processObserver)

        ManagerFactory.getAudioEditorManager().getListCuttingItems()
            .observe(viewLifecycleOwner, listAudioObserver)

        binding.pbResult.max = 100

        binding.btnDelete.setOnClickListener(View.OnClickListener {
            audioStatus?.let {
                if (audioStatus == ConvertingState.PROGRESSING) {
                    ManagerFactory.getAudioEditorManager().cancel(audioId)
                }
            }
        })
    }


}
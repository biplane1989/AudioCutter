package com.example.audiocutter.functions.mystudio.screens

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.base.SingleLiveEvent
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.objects.ActionData

class MyAudioManagerViewModel : BaseViewModel() {
    private var audioCutterSortValue = SortValue(SortType.ASC, SortField.SORT_BY_NAME)
    private var audioMergerSortValue = SortValue(SortType.ASC, SortField.SORT_BY_NAME)
    private var audioMixerSortValue = SortValue(SortType.ASC, SortField.SORT_BY_NAME)
    private val _actionLiveData = SingleLiveEvent<ActionData?>()
    val actionLiveData: LiveData<ActionData?> = _actionLiveData


    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        super.onReceivedAction(fragmentMeta)

        fragmentMeta.data?.let {
            when (fragmentMeta.action) {
                Constance.ACTION_DELETE -> {
                    val data = fragmentMeta.data as Int
                    _actionLiveData.value = ActionData(Constance.ACTION_DELETE, data)
                }
            }
        }
    }

    fun getSortValue(tabPositionSelected: Int): SortValue {
        return when (tabPositionSelected) {
            Constance.AUDIO_CUTTER -> {
                audioCutterSortValue
            }
            Constance.AUDIO_MERGER -> {
                audioMergerSortValue
            }
            else -> {
                audioMixerSortValue
            }

        }
    }

    fun changeSortValue(tabPositionSelected: Int, sortValue: SortValue) {
        when (tabPositionSelected) {
            Constance.AUDIO_CUTTER -> {
                audioCutterSortValue = sortValue
            }
            Constance.AUDIO_MERGER -> {
                audioMergerSortValue = sortValue
            }
            Constance.AUDIO_MIXER -> {
                audioMixerSortValue = sortValue
            }
        }
    }
}
package com.example.audiocutter.core.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.functions.resultscreen.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


object  FakeAudioEditorManager : AudioEditorManager {
    private val listConvertingItemData = ArrayList<ConvertingItem>()
    private val listConvertingItems = MutableLiveData<List<ConvertingItem>>()  // list chung
    private var currConvertingId = 0
    private val mainScope = MainScope()
    private val currentProcessingItem = MutableLiveData<ConvertingItem>()

    private fun getProcessingItem(): ConvertingItem? {
        return null
    }

    private fun getWaitingItem(): ConvertingItem? {
        return null
    }

    override fun cutAudio(audioFile: AudioFile, cuttingConfig: CuttingConfig, outFile: File) {

        val item = CuttingConvertingItem(currConvertingId, ConvertingState.WAITING, 0, outFile, audioFile, cuttingConfig)
        listConvertingItemData.add(item)
        currConvertingId++
        val processingItem = getProcessingItem()
        if (processingItem == null) {
            processNextItem()
        } else {

        }
        listConvertingItems.postValue(listConvertingItemData)
    }

    private fun processNextItem() {
        val waitingItem = getWaitingItem()
        if (waitingItem == null) {
            // TODO khi khoong coon item nao can xu ly
        } else {
            waitingItem.state = ConvertingState.PROGRESSING
            mainScope.launch {
                processItem(waitingItem)
            }

        }
    }

    private fun notifyConvertingItemChanged(item: ConvertingItem) {
        currentProcessingItem.postValue(item)
        listConvertingItems.postValue(listConvertingItemData)
    }

    private suspend fun processItem(item: ConvertingItem) = withContext(Dispatchers.Default) {
        item.percent = 0
        notifyConvertingItemChanged(item)
        (0..100).forEach {
            delay(100)
            item.percent += 1
            notifyConvertingItemChanged(item)
        }
        item.state = ConvertingState.SUCCESS
        notifyConvertingItemChanged(item)
        processNextItem()
    }

    override fun mixAudio(audioFile1: AudioFile, audioFile2: AudioFile, mixingConfig: MixingConfig, outFile: File) {
        TODO("Not yet implemented")
    }

    override fun mergeAudio(listAudioFiles: List<AudioFile>, mergingConfig: MergingConfig, outFile: File) {
        TODO("Not yet implemented")
    }

    override fun cancel(int: Int) {
        TODO("Not yet implemented")
    }


    override fun getCurrentProcessingItem(): LiveData<ConvertingItem> { // tra ra live data cho update progressbar
        return currentProcessingItem
    }

    override fun getListCuttingItems(): LiveData<List<ConvertingItem>> {  // tra live data cho list pending cho cutting
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is CuttingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }

    override fun getListMergingItems(): LiveData<List<ConvertingItem>> {
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is MergingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }

    override fun getListMixingItems(): LiveData<List<ConvertingItem>> {
        return Transformations.map(listConvertingItems) {
            val listItem = ArrayList<ConvertingItem>()
            it.forEach {
                if (it is MixingConvertingItem) {
                    listItem.add(it)
                }
            }
            listItem
        }
    }
}
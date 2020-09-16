package com.example.audiocutter.functions.mystudio.audiocutter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.objects.AudioFile

class AudioCutterViewModel : BaseViewModel() {

    private var mListAudioFileView = ArrayList<AudioFileView>()


    suspend fun getData(): LiveData<List<AudioFileView>> {
        return Transformations.map(
            ManagerFactory.getAudioFileManager().getListAudioCutter()
        ) { listAudioFiles ->
            val listAudioFileView = ArrayList<AudioFileView>()
            //if (mListAudioFileView.size == 0) {
            listAudioFiles.forEach {
                listAudioFileView.add(AudioFileView(it))
            }
            /*} else {

            }*/
            mListAudioFileView = listAudioFileView
            mListAudioFileView
        }
    }

    fun checkItemPosition(pos: Int): List<AudioFileView> {
        val audioFileView = mListAudioFileView.get(pos).copy()
        if (audioFileView.deleteState == DeleteState.UNCHECK) {
            audioFileView.deleteState = DeleteState.CHECKED
        } else {
            audioFileView.deleteState = DeleteState.UNCHECK
        }
        mListAudioFileView[pos] = audioFileView
        return mListAudioFileView
    }

    //
    fun changeAutoItemToDelete(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()
            audioFileView.deleteState = DeleteState.UNCHECK
            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    fun changeAutoItemToMore(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()
            audioFileView.deleteState = DeleteState.HIDE
            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    fun isAllChecked(): Boolean {
        mListAudioFileView.forEach {
            if (it.deleteState == DeleteState.UNCHECK) {
                return false
            }
        }
        return true
    }

    fun clickSelectAllBtn(): List<AudioFileView> {
        if (isAllChecked()) {
            return unselectAllItems()
        } else {
            return selectAllItems()
        }
    }

    private fun selectAllItems(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()
            audioFileView.deleteState = DeleteState.CHECKED
            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    private fun unselectAllItems(): List<AudioFileView> {
        val copy = ArrayList<AudioFileView>()
        mListAudioFileView.forEach {
            val audioFileView = it.copy()
            audioFileView.deleteState = DeleteState.UNCHECK
            copy.add(audioFileView)
        }

        mListAudioFileView = copy
        return mListAudioFileView
    }

    fun deleteAllItemSelected() {
        val listAudioItems = ArrayList<AudioFile>()
        mListAudioFileView.forEach {
            if (it.deleteState == DeleteState.CHECKED) {
                listAudioItems.add(it.audioFile)
            }
        }
        runOnBackground {
            ManagerFactory.getAudioFileManager().deleteFile(listAudioItems)
        }
    }

    fun showPlayingAudio(position: Int): List<AudioFileView> {
        var index = 0
        for (item in mListAudioFileView) {
            if (index != position) {
                val newItem = item.copy()
                newItem.isExpanded = false
                mListAudioFileView[index] = newItem
            }
            index++
        }
        if (mListAudioFileView.get(position).isExpanded) {
            val audioFileView = mListAudioFileView.get(position).copy()
            audioFileView.isExpanded = false
            mListAudioFileView.set(position, audioFileView)
        } else {
            val audioFileView = mListAudioFileView.get(position).copy()
            audioFileView.isExpanded = true
            mListAudioFileView.set(position, audioFileView)
        }
        return mListAudioFileView
    }

}
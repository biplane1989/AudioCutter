package com.example.audiocutter.functions.contactscreen.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.audiocutter.base.BaseViewModel
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.functions.mystudioscreen.AudioFileView
import com.example.audiocutter.functions.mystudioscreen.Constance
import com.example.audiocutter.functions.mystudioscreen.DeleteState
import com.example.audiocutter.objects.AudioFile

class ListSelectAudioViewModel : BaseViewModel() {

    private var mListAudioFileView = ArrayList<SelectItemView>()


    suspend fun getData(): LiveData<List<SelectItemView>> {
        val listAudioFiles: LiveData<List<AudioFile>>
        listAudioFiles = ManagerFactory.getAudioFileManager()
            .findAllAudioFiles()
        return Transformations.map(listAudioFiles) { items ->
            // lan dau tien lay du lieu
            if (mListAudioFileView.size == 0) {
                items.forEach {
                    mListAudioFileView.add(SelectItemView(it))
                }

            } else { // khi thay doi du lieu update
                // đồng bộ hóa list cũ và mới
                val newListSelectItemView = ArrayList<SelectItemView>()
                items.forEach {
                    val audioFileView = getAudioFileView(it.file.absolutePath)
                    if (audioFileView != null) {
                        newListSelectItemView.add(audioFileView)
                    } else {
//                        if (isDeleteStatus) {
//                            val newAudioFileView = SelectItemView(it)
//                            newAudioFileView.itemLoadStatus.deleteState = DeleteState.UNCHECK
//                            newListSelectItemView.add(newAudioFileView)
//
//
//                        } else {
//                            newListSelectItemView.add(SelectItemView(it))
//                        }
                    }
                }
                mListAudioFileView = newListSelectItemView
            }
            mListAudioFileView
        }
    }

    // tìm ra những file đã tồn tại trong list cũ
    private fun getAudioFileView(filePath: String): SelectItemView? {
        mListAudioFileView.forEach {
            if (it.audioFile.file.absolutePath.equals(filePath)) {
                return it
            }
        }
        return null
    }


}
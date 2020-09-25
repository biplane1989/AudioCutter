package com.example.audiocutter.functions.contactscreen.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.mystudioscreen.fragment.AudioCutterAdapter
import com.example.audiocutter.functions.mystudioscreen.fragment.MyStudioViewModel
import kotlinx.android.synthetic.main.list_contact_select_screen.*
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.delay

class ListSelectAudioScreen : BaseFragment(), SelectAudioScreenCallback {


    lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    lateinit var mListSelectAdapter: ListSelectAdapter

    // observer data
    val listAudioObserver = Observer<List<SelectItemView>> { listMusic ->
        mListSelectAdapter.submitList(ArrayList(listMusic))

    }

//    // observer playInfo mediaplayer
//    private val playerInfoObserver = Observer<PlayerInfo> {
////        if (mListSelectAudioViewModel.isPlayingStatus) {
////            mListSelectAdapter.submitList(mListSelectAudioViewModel.updatePlayerInfo(it))
////        }
//
//    }

    fun init() {
        rv_list_select_audio.layoutManager = LinearLayoutManager(context)
        rv_list_select_audio.setHasFixedSize(true)
        rv_list_select_audio.adapter = mListSelectAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_contact_select_screen, container, false)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mListSelectAudioViewModel = ViewModelProviders.of(this)
            .get(ListSelectAudioViewModel::class.java)
        mListSelectAdapter = ListSelectAdapter(this)

        runOnUI {
            val listSelectViewLiveData = mListSelectAudioViewModel.getData() // get data from funtion newIntance
            listSelectViewLiveData.observe(this as LifecycleOwner, listAudioObserver)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun play(position: Int) {
        TODO("Not yet implemented")
    }

    override fun pause(position: Int) {
        TODO("Not yet implemented")
    }

    override fun resume(position: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(position: Int) {
        TODO("Not yet implemented")
    }

    override fun seekTo(cusorPos: Int) {
        TODO("Not yet implemented")
    }

}
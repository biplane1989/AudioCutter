package com.example.audiocutter.functions.contactscreen.select

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import kotlinx.android.synthetic.main.list_contact_select_screen.*


class ListSelectAudioScreen() : BaseFragment(), SelectAudioScreenCallback {

    val TAG = "giangtd"
    lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    lateinit var mListSelectAdapter: ListSelectAdapter

    companion object {
        val TAG = "ListSelectAudioScreen"
        val BUNDLE_NAME_KEY_PHONE_NUMBER = "BUNDLE_NAME_KEY_PHONE_NUMBER"
        val BUNDLE_NAME_KEY_URI = "BUNDLE_NAME_KEY_URI"

        @JvmStatic
        fun newInstance(phoneNumber: String, uri: String): ListSelectAudioScreen {
            val MyStudio = ListSelectAudioScreen()
            val bundle = Bundle()
            bundle.putString(BUNDLE_NAME_KEY_PHONE_NUMBER, phoneNumber)
            bundle.putString(BUNDLE_NAME_KEY_URI, uri)
            MyStudio.arguments = bundle
            return MyStudio
        }
    }

    // observer data
    val listAudioObserver = Observer<List<SelectItemView>> { listAudio ->
        if (listAudio.size <= 0) {
            rv_list_select_audio.visibility = View.GONE
            cl_bottom.visibility = View.GONE
            cl_no_audio.visibility = View.VISIBLE
        } else {
            rv_list_select_audio.visibility = View.VISIBLE
            cl_bottom.visibility = View.VISIBLE
            cl_no_audio.visibility = View.GONE
            mListSelectAdapter.submitList(ArrayList(listAudio))
        }

    }

    // observer playInfo mediaplayer
    private val playerInfoObserver = Observer<PlayerInfo> {
        if (mListSelectAudioViewModel.isPlayingStatus) {
            mListSelectAdapter.submitList(mListSelectAudioViewModel.updatePlayerInfo(it))
        }

    }

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

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
        runOnUI {
            val listSelectViewLiveData = mListSelectAudioViewModel.getData() // get data from funtion newIntance
            listSelectViewLiveData.observe(this as LifecycleOwner, listAudioObserver)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        iv_search.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.GONE
            cl_search.visibility = View.VISIBLE
        })

        iv_search_close.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.VISIBLE
            cl_search.visibility = View.GONE
        })

        iv_clear.setOnClickListener(View.OnClickListener {
            edt_search.text.clear()
        })

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mListSelectAudioViewModel.searchAudioFile(edt_search.text.toString()).size <= 0) {
                    rv_list_select_audio.visibility = View.GONE
                    cl_bottom.visibility = View.GONE
                    cl_no_audio.visibility = View.VISIBLE
                } else {
                    rv_list_select_audio.visibility = View.VISIBLE
                    cl_bottom.visibility = View.VISIBLE
                    cl_no_audio.visibility = View.GONE
                    mListSelectAdapter.submitList(mListSelectAudioViewModel.searchAudioFile(edt_search.text.toString()))
                }
            }
        })

        btn_save.setOnClickListener(View.OnClickListener {
            if (mListSelectAudioViewModel.setRingtone("001")) {
                Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
            }
        })

       iv_file.setOnClickListener(View.OnClickListener {
//           val intent = Intent(Intent.ACTION_GET_CONTENT)
//           intent.type = "file/*"
//           startActivityForResult(intent, PICKFILE_REQUEST_CODE)
       })
    }

    override fun play(position: Int) {
        mListSelectAudioViewModel.playAudio(position)
        Log.d(TAG, "play: ")
    }

    override fun pause() {
        mListSelectAudioViewModel.pauseAudio()
    }

    override fun resume() {
        mListSelectAudioViewModel.resumeAudio()
    }

    override fun stop(position: Int) {
        mListSelectAudioViewModel.stopAudio(position)
    }

    override fun seekTo(cusorPos: Int) {
        mListSelectAudioViewModel.seekToAudio(cusorPos)
    }

    override fun isShowPlayingAudio(positition: Int) {
        mListSelectAdapter.submitList(mListSelectAudioViewModel.showPlayingAudio(positition))
    }

    override fun isSelect(position: Int) {
        mListSelectAdapter.submitList(mListSelectAudioViewModel.selectAudio(position))
    }

}
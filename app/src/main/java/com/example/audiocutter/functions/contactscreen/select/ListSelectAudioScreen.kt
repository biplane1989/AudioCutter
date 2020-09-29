package com.example.audiocutter.functions.contactscreen.select

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
import com.example.audiocutter.functions.mystudioscreen.fragment.MyStudioFragment
import kotlinx.android.synthetic.main.list_contact_select_screen.*


class ListSelectAudioScreen() : BaseFragment(), SelectAudioScreenCallback, View.OnClickListener {

    val TAG = "giangtd"
    lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    lateinit var mListSelectAdapter: ListSelectAdapter

    companion object {
        val TAG = "ListSelectAudioScreen"
        val BUNDLE_NAME_KEY_PHONE_NUMBER = "BUNDLE_NAME_KEY_PHONE_NUMBER"
        val BUNDLE_NAME_KEY_FILE_NAME = "BUNDLE_NAME_KEY_URI"

        @JvmStatic
        fun newInstance(phoneNumber: String, uri: String): ListSelectAudioScreen {
            val MyStudio = ListSelectAudioScreen()
            val bundle = Bundle()
            bundle.putString(BUNDLE_NAME_KEY_PHONE_NUMBER, phoneNumber)
            bundle.putString(BUNDLE_NAME_KEY_FILE_NAME, uri)
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
            runOnUI {
                rv_list_select_audio.visibility = View.VISIBLE
                cl_bottom.visibility = View.VISIBLE
                cl_no_audio.visibility = View.GONE
                mListSelectAdapter.submitList(ArrayList(listAudio))

                val fileName = requireArguments().getString(BUNDLE_NAME_KEY_FILE_NAME)

                if (fileName != null) {
                    mListSelectAdapter.submitList(mListSelectAudioViewModel.setSelectRingtone(fileName))
                    rv_list_select_audio.scrollToPosition(mListSelectAudioViewModel.getIndexSelectRingtone(fileName))
                }
            }
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

        iv_search.setOnClickListener(this)
        iv_search_close.setOnClickListener(this)
        iv_clear.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        iv_file.setOnClickListener(this)

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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_search -> {
                cl_default.visibility = View.GONE
                cl_search.visibility = View.VISIBLE
            }
            R.id.iv_search_close -> {
                cl_default.visibility = View.VISIBLE
                cl_search.visibility = View.GONE
            }
            R.id.iv_clear -> {
                edt_search.text.clear()
            }
            R.id.btn_save -> {
                if (mListSelectAudioViewModel.setRingtone(requireArguments().getInt(BUNDLE_NAME_KEY_PHONE_NUMBER)
                        .toString())) {
                    Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_file -> {
                //           val intent = Intent(Intent.ACTION_GET_CONTENT)
//           intent.type = "file/*"
//           startActivityForResult(intent, PICKFILE_REQUEST_CODE)
            }
        }
    }

}
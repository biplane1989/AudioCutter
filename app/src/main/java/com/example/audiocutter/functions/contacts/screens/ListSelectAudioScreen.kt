package com.example.audiocutter.functions.contacts.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.ListContactSelectScreenBinding
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.contacts.adapters.SelectAudioScreenCallback
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.util.FileUtils
import kotlinx.android.synthetic.main.list_contact_select_screen.*


class ListSelectAudioScreen() : BaseFragment(),
    SelectAudioScreenCallback, View.OnClickListener {
    val TAG = "giangtd"
    val safeArg: ListSelectAudioScreenArgs by navArgs()
    lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    lateinit var mListSelectAdapter: ListSelectAdapter
    var isLoading = false   // trang thai load cua progressbar
    var currentView: View? = null
    val REQ_CODE_PICK_SOUNDFILE = 1989
    var positionSelect = -1
    private var listSelectAudio: LiveData<List<SelectItemView>>? = null
    lateinit var binding: ListContactSelectScreenBinding

    // observer data
    val listAudioObserver = Observer<List<SelectItemView>> { listAudio ->
        Log.d(TAG, "list audio size : " + listAudio.size)
        if (listAudio != null) {
            if (listAudio.isEmpty()) {
                cl_select.visibility = View.GONE
                cl_bottom.visibility = View.GONE
                cl_no_audio.visibility = View.VISIBLE
            } else {
                runOnUI {
                    pb_select.visibility = View.GONE
                    cl_select.visibility = View.VISIBLE
                    cl_bottom.visibility = View.VISIBLE
                    cl_no_audio.visibility = View.GONE
                    mListSelectAdapter.submitList(ArrayList(listAudio))

                    val fileName = safeArg.uri            // tam thoi comment
//
                    if (fileName != null) {
                        mListSelectAdapter.submitList(
                            mListSelectAudioViewModel.setSelectRingtone(
                                fileName
                            )
                        )
                        rv_list_select_audio.scrollToPosition(
                            mListSelectAudioViewModel.getIndexSelectRingtone(
                                fileName
                            )
                        )
                    }
                }
            }
        } else {
            Log.d(TAG, "audio: is null")
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.list_contact_select_screen, container, false)
        listSelectAudio?.observe(this.viewLifecycleOwner, listAudioObserver)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mListSelectAudioViewModel = ViewModelProviders.of(this)
            .get(ListSelectAudioViewModel::class.java)
        mListSelectAdapter =
            ListSelectAdapter(
                this
            )

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
        isLoading = true
        runOnUI {
            listSelectAudio =
                mListSelectAudioViewModel.getData() // get data from funtion newIntance
            listSelectAudio?.observe(this.viewLifecycleOwner, listAudioObserver)
            isLoading = false
            currentView?.findViewById<ProgressBar>(R.id.pb_select)?.visibility =
                View.GONE    // tai day ham onCreateView da chay xong r do runOnUI

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.ivFile.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)

        if (isLoading) {
            pb_select.visibility = View.VISIBLE
        } else {
            pb_select.visibility = View.GONE
        }

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(
                textChange: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (mListSelectAudioViewModel.searchAudioFile(textChange.toString()).size <= 0) {
                    cl_select.visibility = View.GONE
                    cl_bottom.visibility = View.GONE
                    cl_no_audio.visibility = View.VISIBLE
                } else {
                    cl_select.visibility = View.VISIBLE
                    cl_bottom.visibility = View.VISIBLE
                    cl_no_audio.visibility = View.GONE
                    mListSelectAdapter.submitList(
                        mListSelectAudioViewModel.searchAudioFile(
                            textChange.toString()
                        )
                    )
                }
            }
        })
    }


    override fun play(position: Int) {
        mListSelectAudioViewModel.playAudio(position)
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
        positionSelect = positition
        mListSelectAdapter.submitList(mListSelectAudioViewModel.showPlayingAudio(positition))
    }

    override fun isSelect(position: Int) {

        mListSelectAdapter.submitList(mListSelectAudioViewModel.selectAudio(position))
    }


    override fun onClick(view: View?) {
        when (view) {
            binding.ivSearch -> {
                showKeyboard()
                cl_default.visibility = View.GONE
                cl_search.visibility = View.VISIBLE
            }
            binding.ivSearchClose -> {
                cl_default.visibility = View.VISIBLE
                cl_search.visibility = View.GONE
                edt_search.text.clear()
                hideKeyboard()
            }
            binding.ivClear -> {
                edt_search.text.clear()
            }
            binding.btnSave -> {
                if (mListSelectAudioViewModel.setRingtone(safeArg.phoneNumber)) {
                    Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
                }
                requireActivity().onBackPressed()
            }
            binding.ivFile -> {
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                }/* else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                } */
                else {
                    intent = Intent(Intent.ACTION_GET_CONTENT)
                }
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.type = "audio/*"
//                intent.type = "audio/mp3"
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(
                    Intent.createChooser(intent, "Select a File "),
                    REQ_CODE_PICK_SOUNDFILE
                )

            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
        }
    }

    //    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK && intent != null) {

            val path = FileUtils.getPath(requireContext(), intent.data!!)

            path?.let {
                if (mListSelectAudioViewModel.setRingtoneWithUri(safeArg.phoneNumber, path)) {
                    Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        edt_search.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onPause() {
        super.onPause()
        if (positionSelect > -1) {
            mListSelectAudioViewModel.stopAudio(positionSelect)
        }
    }
}
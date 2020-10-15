package com.example.audiocutter.functions.audiochooser.screens

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.audioManager.Folder
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.CutChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.AudiocutterAdapter
import com.example.audiocutter.functions.audiochooser.dialogs.SetAsDialog
import com.example.audiocutter.functions.audiochooser.dialogs.SetAsDoneDialog
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.objects.TypeAudioSetAs
import kotlinx.coroutines.delay

class CutChooserScreen : BaseFragment(), AudiocutterAdapter.AudioCutterListener,
    SetAsDialog.setAsListener, View.OnClickListener {
    val TAG = CutChooserScreen::class.java.name
    private lateinit var binding: CutChooserScreenBinding
    private lateinit var audioCutterAdapter: AudiocutterAdapter
    private lateinit var audioCutterModel: AudioCutterModel
    lateinit var dialog: SetAsDialog
    lateinit var dialogDone: SetAsDoneDialog
    lateinit var audioCutterItem: AudioCutterView
    var stateObserver = Observer<Boolean> {
        if (it) {
            showProgressBar(true)
        } else {
            showProgressBar(false)
        }
    }

    var currentPos = -1


    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        if (listMusic.isEmpty()) {
            showEmptyView()
        }
        audioCutterAdapter.submitList(ArrayList(listMusic))
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioCutterAdapter.submitList(audioCutterModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioCutterAdapter = AudiocutterAdapter(requireContext())
        audioCutterModel = ViewModelProvider(this).get(AudioCutterModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.cut_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        showProgressBar(true)
        runOnUI {
            delay(500)
            val listAudioViewLiveData = audioCutterModel.getAllAudioFile()
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)

            val stateLiveData = audioCutterModel.getStateLoading()
            stateLiveData.observe(viewLifecycleOwner, stateObserver)
        }
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioCutter.visibility = View.VISIBLE
        } else {
            binding.pgrAudioCutter.visibility = View.GONE
        }
    }

    private fun showEmptyView() {
        binding.rvAudioCutter.visibility = View.INVISIBLE
        binding.ivEmptyListCutter.visibility = View.VISIBLE
        binding.tvEmptyListCutter.visibility = View.VISIBLE
        showProgressBar(false)
    }


    private fun checkEdtSearchAudio() {
        binding.edtCutterSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchAudioByName(binding.edtCutterSearch.text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchAudioByName(yourTextSearch: String) {

        binding.rvAudioCutter.visibility = View.VISIBLE
        binding.tvEmptyListCutter.visibility = View.GONE
        binding.ivEmptyListCutter.visibility = View.GONE

        if (yourTextSearch.isEmpty()) {
            audioCutterAdapter.submitList(audioCutterModel.getListAudio())
        }
        if (audioCutterModel.searchAudio(audioCutterModel.getListAudio(), yourTextSearch)
                .isNotEmpty()
        ) {
            audioCutterAdapter.submitList(audioCutterModel.getListsearch())
            Log.d(TAG, "seachAudioByName: ${audioCutterModel.getListsearch().size}")
        } else {
            binding.rvAudioCutter.visibility = View.GONE
            binding.tvEmptyListCutter.visibility = View.VISIBLE
            binding.ivEmptyListCutter.visibility = View.VISIBLE
        }
    }


    private fun initViews() {
        binding.ivCutterScreenBack.setOnClickListener(this)
        binding.ivAudioCutterScreenFile.setOnClickListener(this)
        binding.ivCutterScreenSearch.setOnClickListener(this)
        binding.ivCutterScreenBackEdt.setOnClickListener(this)
        binding.ivCutterScreenClose.setOnClickListener(this)
        dialog = SetAsDialog(requireContext())
        dialogDone = SetAsDoneDialog(requireContext())

        audioCutterAdapter.setAudioCutterListtener(this)

    }

    private fun hideOrShowEditText(status: Int) {
        binding.ivCutterScreenBackEdt.visibility = status
        binding.ivCutterScreenClose.visibility = status
        binding.edtCutterSearch.visibility = status
    }

    private fun hideOrShowView(status: Int) {
        binding.ivCutterScreenSearch.visibility = status
        binding.tvCutterScreen.visibility = status
        binding.ivAudioCutterScreenFile.visibility = status
    }


    private fun hideKeyBroad() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeybroad() {
        binding.edtCutterSearch.requestFocus()
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


    private fun initLists() {
        binding.rvAudioCutter.adapter = audioCutterAdapter
        binding.rvAudioCutter.setHasFixedSize(true)
        binding.rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())
    }


    override fun play(pos: Int) {
        runOnUI {
            currentPos = pos
            audioCutterModel.play(pos)
        }
    }

    override fun pause(pos: Int) {
        currentPos = pos
        audioCutterModel.pause()

    }

    override fun resume(pos: Int) {
        currentPos = pos
        audioCutterModel.resume()
    }


    override fun showDialogSetAs(itemAudio: AudioCutterView) {
        audioCutterItem = itemAudio
        dialog.setOnCallBack(this)
        dialog.show()
    }



    override fun setAudioAs(typeAudioSetAs: TypeAudioSetAs) {
        var rs = false
        Log.d(TAG, "setAudioAs: ${audioCutterItem.audioFile.fileName}")
        when (typeAudioSetAs) {

            TypeAudioSetAs.RINGTONE -> {
                rs = ManagerFactory.getRingtonManager().setRingTone(audioCutterItem.audioFile)
            }
            TypeAudioSetAs.ALARM -> {
                rs = ManagerFactory.getRingtonManager().setAlarmManager(
                    audioCutterItem.audioFile
                )
            }
            TypeAudioSetAs.NOTIFICATION -> {
                rs = ManagerFactory.getRingtonManager()
                    .setNotificationSound(audioCutterItem.audioFile)
            }
        }
        if (rs) {
            dialog.dismiss()
            dialogDone.show()
        } else {
            Toast.makeText(requireContext(), "set as fail", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivAudioCutterScreenFile -> {
                updateAllFile()
            }
            binding.ivCutterScreenSearch -> {
                searchAudiofile()
            }
            binding.ivCutterScreenBackEdt -> {
                previousStatus()
            }
            binding.ivCutterScreenClose -> {
                clearText()
            }
            binding.ivCutterScreenBack -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun clearText() {
        if (binding.edtCutterSearch.text.toString().isNotEmpty()) {
            binding.edtCutterSearch.setText("")
        }
    }

    private fun previousStatus() {
        hideKeyBroad()
        binding.rvAudioCutter.visibility = View.VISIBLE
        binding.tvEmptyListCutter.visibility = View.GONE
        binding.ivEmptyListCutter.visibility = View.GONE
        audioCutterAdapter.submitList(audioCutterModel.getListAudio())
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        showKeybroad()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }


    private fun updateAllFile() {
        ManagerFactory.getAudioFileManager().getListAudioFileByType(Folder.TYPE_CUTTER)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getAudioPlayer().stop()
    }

}





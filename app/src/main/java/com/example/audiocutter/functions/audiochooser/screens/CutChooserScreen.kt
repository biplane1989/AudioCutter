package com.example.audiocutter.functions.audiochooser.screens

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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.CutChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.CutChooserAdapter
import com.example.audiocutter.functions.audiochooser.dialogs.SetAsDialog
import com.example.audiocutter.functions.audiochooser.dialogs.SetAsDoneDialog
import com.example.audiocutter.functions.audiochooser.event.OnActionCallback
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.functions.audiochooser.objects.TypeAudioSetAs
import com.example.audiocutter.util.FileUtils

class CutChooserScreen : BaseFragment(), CutChooserAdapter.CutChooserListener, SetAsDialog.setAsListener, View.OnClickListener, OnActionCallback {
    val TAG = CutChooserScreen::class.java.name
    private lateinit var binding: CutChooserScreenBinding
    private lateinit var audioCutterAdapter: CutChooserAdapter
    private lateinit var audioCutterModel: AudioCutterModel
    private val REQ_CODE_PICK_SOUNDFILE = 1989
    lateinit var dialog: SetAsDialog
    lateinit var dialogDone: SetAsDoneDialog
    lateinit var audioCutterItem: AudioCutterView

    var stateObserver = Observer<Int> {
        Log.d("nqm", "stateObserver: $it")
        when (it) {
            1 -> {
                Log.d("nqm", "stateObserver: 1")
                showProgressBar(true)
                binding.ivEmptyListCutter.visibility = View.GONE
                binding.tvEmptyListCutter.visibility = View.GONE
            }
            0 -> {
                Log.d("nqm", "stateObserver: 0")
                showProgressBar(false)
            }
            -1 -> {
                Log.d("nqm", "stateObserver: -1")
                showProgressBar(false)
            }
        }
    }
    var currentPos = -1

    private val listAudioObserver = Observer<List<AudioCutterView>> { listMusic ->
        Log.d("nqm", "listMusic: ${listMusic.size}")
        if (listMusic.isEmpty() || listMusic == null) {
            showEmptyList()
        } else {
            audioCutterAdapter.submitList(ArrayList(listMusic))
            showList()
        }
    }


    private val playerInfoObserver = Observer<PlayerInfo> {
        audioCutterAdapter.submitList(audioCutterModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioCutterAdapter = CutChooserAdapter(requireContext())
        audioCutterModel = ViewModelProvider(this).get(AudioCutterModel::class.java)
        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.cut_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        runOnUI {
            val stateLiveData = audioCutterModel.getStateLoading()
            stateLiveData.observe(viewLifecycleOwner, stateObserver)

            val listAudioViewLiveData = audioCutterModel.getAllAudioFile()
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)

        }
    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioCutter.visibility = View.VISIBLE
        } else {
            binding.pgrAudioCutter.visibility = View.GONE
        }
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
                .isNotEmpty()) {
            audioCutterAdapter.submitList(audioCutterModel.getListsearch())
            Log.d(TAG, "seachAudioByName: ${audioCutterModel.getListsearch().size}")
        } else {
            binding.rvAudioCutter.visibility = View.INVISIBLE
            binding.tvEmptyListCutter.visibility = View.VISIBLE
            binding.ivEmptyListCutter.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        audioCutterModel.pause()
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

    private fun showEmptyList() {
        showProgressBar(false)
        binding.rvAudioCutter.visibility = View.INVISIBLE
        binding.ivEmptyListCutter.visibility = View.VISIBLE
        binding.tvEmptyListCutter.visibility = View.VISIBLE
    }

    private fun showList() {
        showProgressBar(false)
        binding.rvAudioCutter.visibility = View.VISIBLE
        binding.ivEmptyListCutter.visibility = View.INVISIBLE
        binding.tvEmptyListCutter.visibility = View.INVISIBLE
    }


    private fun hideKeyBroad() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeybroad() {
        binding.edtCutterSearch.requestFocus()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_audio_choose_merge, null)
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
        dialog.show(requireActivity().supportFragmentManager, "TAG")
    }

    override fun onCutItemClicked(itemAudio: AudioCutterView) {
        viewStateManager.onCuttingItemClicked(this, itemAudio)

    }

    override fun setRingtoneContact(filePath: String) {
        viewStateManager.onCutScreenSetRingtoneContact(this, filePath)
    }

    override fun setAudioAs(typeAudioSetAs: TypeAudioSetAs) {
        var rs = false
        Log.d(TAG, "setAudioAs: ${audioCutterItem.audioFile.fileName}")
        rs = when (typeAudioSetAs) {

            TypeAudioSetAs.RINGTONE -> {
                ManagerFactory.getRingtonManager().setRingTone(audioCutterItem.audioFile)
            }
            TypeAudioSetAs.ALARM -> {
                ManagerFactory.getRingtonManager().setAlarmManager(audioCutterItem.audioFile)
            }
            TypeAudioSetAs.NOTIFICATION -> {
                ManagerFactory.getRingtonManager().setNotificationSound(audioCutterItem.audioFile)
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
                getAllFile()
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
        binding.edtCutterSearch.setText("")
        hideOrShowEditText(View.GONE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        showKeybroad()
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.GONE)
    }


    private fun getAllFile() {
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
        startActivityForResult(Intent.createChooser(intent, "Select a File "), REQ_CODE_PICK_SOUNDFILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK && intent != null) {

            val path = FileUtils.getPath(requireContext(), intent.data!!)
            path?.let {
                val audio = ManagerFactory.getAudioFileManager().buildAudioFile(path)
                viewStateManager.onCuttingItemClicked(this, AudioCutterView(audio))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getDefaultAudioPlayer().stop()
    }

}







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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.CutChooserScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.CutChooserAdapter
import com.example.audiocutter.functions.audiochooser.adapters.FolderCutChooserAdapter
import com.example.audiocutter.functions.audiochooser.dialogs.SetAsDialog
import com.example.audiocutter.functions.audiochooser.event.OnActionCallback
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.audiochooser.objects.FolderItem
import com.example.audiocutter.functions.audiochooser.objects.FolderStatus
import com.example.audiocutter.functions.audiochooser.objects.TypeAudioSetAs
import com.example.audiocutter.functions.common.*
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.ContactItemPermissionRequest
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.permissions.WriteSettingPermissionRequest
import com.example.audiocutter.util.FileUtils
import com.google.android.material.snackbar.Snackbar

class CutChooserScreen : BaseFragment(), CutChooserAdapter.CutChooserListener, SetAsDialog.setAsListener, View.OnClickListener, OnActionCallback {

    private var positionRv = 0
    private var filePathAudio: String? = ""
    val TAG = CutChooserScreen::class.java.name
    private lateinit var binding: CutChooserScreenBinding
    private lateinit var audioCutterAdapter: CutChooserAdapter
    private lateinit var audioCutterModel: CutChooserViewModel
    private val REQ_CODE_PICK_SOUNDFILE = 1989
    private lateinit var dialog: SetAsDialog

    //    private lateinit var dialogDone: SetAsDoneDialog
    private lateinit var audioCutterItem: AudioCutterViewItem
    private var pendingRequestingPermission = 0
    private val WRITESETTING_ITEM_REQUESTING_PERMISSION = 1 shl 5
    private var indexChoose = 0
    private var isSearchStatus = false

    private val folderAdapter: FolderCutChooserAdapter by lazy {
        FolderCutChooserAdapter(this::clickFolderItem)
    }

    private var stateObserver = Observer<Int> {
        when (it) {
            1 -> {
                showProgressBar(true)
            }
            0 -> {
                showProgressBar(false)
            }
            -1 -> {
                showProgressBar(false)
            }
        }
    }

    private val listAudioObserver = Observer<List<AudioCutterViewItem>?> { listMusic ->
        listMusic?.forEach {

            Log.d(TAG, "checkListMusic:${it.audioFile.fileName} ")
        }
        if (listMusic == null) {
            binding.rvAudioCutter.visibility = View.INVISIBLE
        } else {
            if (listMusic.isEmpty()) {
                showEmptyList()
            } else {
                audioCutterAdapter.submitList(ArrayList(listMusic)) {}
                showList()
                showProgressBar(false)

            }
        }
    }

    private val folderObserver = Observer<List<FolderItem>> {
        folderAdapter.submitList(it)
    }

    private val folderStatusObserver = Observer<FolderStatus> {
        if (it.status) {
            binding.rvAudioCutter.visibility = View.INVISIBLE
            audioCutterAdapter.submitList(emptyList())
            binding.rvFolderCutter.visibility = View.VISIBLE
            binding.ivCutterScreenSearch.visibility = View.INVISIBLE
        } else {
            binding.rvFolderCutter.visibility = View.INVISIBLE
            binding.rvAudioCutter.visibility = View.VISIBLE
            binding.ivCutterScreenSearch.visibility = View.VISIBLE
        }

        binding.tvCutterScreen.text = it.folder
    }

    private val writeSettingPermissionRequest = object : WriteSettingPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    private val contactPermissionRequest = object : ContactItemPermissionRequest {
        override fun getPermissionActivity(): BaseActivity? {
            return getBaseActivity()
        }

        override fun getLifeCycle(): Lifecycle {
            return lifecycle
        }
    }

    private val emptyState = Observer<Boolean> {
        if (it) {
            showList()
        } else {
            showEmptyList()
        }
    }

    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            binding.rvAudioCutter.scrollToPosition(0)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            binding.rvAudioCutter.scrollToPosition(0)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            binding.rvAudioCutter.scrollToPosition(0)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioCutterModel = ViewModelProvider(this).get(CutChooserViewModel::class.java)
        audioCutterAdapter = CutChooserAdapter(requireContext(), audioCutterModel.getAudioPlayer(), lifecycleScope, requireActivity())
//        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.cut_chooser_screen, container, false)
        initViews()
        checkEdtSearchAudio()

          PermissionManager.getAppPermission()
              .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                  if (contactPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and WRITESETTING_ITEM_REQUESTING_PERMISSION) != 0) {
                      resetRequestingPermission()
                      requestPermissinWriteSetting()
                      if (writeSettingPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and WRITESETTING_ITEM_REQUESTING_PERMISSION) != 0) {
                          showDialogSetAsTypeAudio()
                      }
                  }


              })

        binding.rvAudioCutter.scrollToPosition(positionRv)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
        observerData()
    }

    private fun observerData() {
        audioCutterModel.getStateLoading().observe(viewLifecycleOwner, stateObserver)
        audioCutterModel.listAudioCutterViewItems.observe(viewLifecycleOwner, listAudioObserver)
        audioCutterModel.isEmptyState.observe(viewLifecycleOwner, emptyState)

        audioCutterModel.showSortAudioDialog.observe(viewLifecycleOwner) {
            val sortAudioPopupWindow = SortAudioPopupWindow(binding.ivCutterScreenSort, it) {
                audioCutterModel.sortAudioBy(it)
            }
            sortAudioPopupWindow.show()
        }

        audioCutterModel.listFolder.observe(viewLifecycleOwner, folderObserver)
        audioCutterModel.folderLiveData.observe(viewLifecycleOwner, folderStatusObserver)

    }

    private fun showProgressBar(b: Boolean) {
        if (b) {
            binding.pgrAudioCutter.visibility = View.VISIBLE
            binding.linear.visibility = View.INVISIBLE
        } else {
            binding.pgrAudioCutter.visibility = View.INVISIBLE
            binding.linear.visibility = View.VISIBLE
        }
    }

    private fun clickFolderItem(folderItem: FolderItem) {        //click folder item todo
        audioCutterModel.clickItemFolder(folderItem)
    }

    private fun checkEdtSearchAudio() {
        binding.edtCutterSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(textChange: CharSequence?, p1: Int, p2: Int, p3: Int) {

//                binding.rvAudioCutter.post {
//                    binding.rvAudioCutter.smoothScrollToPosition(0)
//                }
                audioCutterModel.stop()
                searchAudioByName(textChange.toString())
                if (textChange.toString() != "") {
                    binding.ivCutterScreenClose.visibility = View.VISIBLE
                } else {
                    binding.ivCutterScreenClose.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun searchAudioByName(yourTextSearch: String) {

        if (yourTextSearch.isEmpty()) {
            audioCutterModel.searchAudio("")
        }
        audioCutterModel.searchAudio(yourTextSearch)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
        audioCutterModel.pause()
    }

    private fun initViews() {
        binding.ivCutterScreenBack.setOnClickListener(this)
        binding.ivAudioCutterScreenFile.setOnClickListener(this)
        binding.ivCutterScreenSearch.setOnClickListener(this)
        binding.ivCutterScreenBackEdt.setOnClickListener(this)
        binding.ivCutterScreenClose.setOnClickListener(this)
        binding.ivCutterScreenSort.setOnClickListener(this)
        binding.linear.setOnClickListener(this)

        dialog = SetAsDialog(requireContext())
//        dialogDone = SetAsDoneDialog(requireContext())
        audioCutterAdapter.setAudioCutterListtener(this)

    }

    private fun hideOrShowEditText(status: Int) {
        binding.ivCutterScreenBackEdt.visibility = status
//        binding.ivCutterScreenClose.visibility = status
        binding.edtCutterSearch.visibility = status

    }

    private fun hideOrShowView(status: Int) {
        binding.ivCutterScreenSearch.visibility = status
        binding.tvCutterScreen.visibility = status
        binding.ivAudioCutterScreenFile.visibility = status
        binding.ivCutterScreenSort.visibility = status
    }

    private fun showEmptyList() {
        showProgressBar(false)
        binding.rvAudioCutter.visibility = View.INVISIBLE
        binding.rvFolderCutter.visibility = View.INVISIBLE
        binding.ivEmptyListCutter.visibility = View.VISIBLE
        binding.tvEmptyListCutter.visibility = View.VISIBLE
    }

    private fun showList() {
        if (audioCutterModel.folderLiveData.value?.status == true) {
            binding.rvFolderCutter.visibility = View.VISIBLE
        } else {
            binding.rvAudioCutter.visibility = View.VISIBLE
        }

        binding.ivEmptyListCutter.visibility = View.INVISIBLE
        binding.tvEmptyListCutter.visibility = View.INVISIBLE
    }


    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtCutterSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun initLists() {
        binding.rvAudioCutter.adapter = audioCutterAdapter
        binding.rvAudioCutter.setHasFixedSize(true)
        binding.rvAudioCutter.layoutManager = LinearLayoutManager(requireContext())

        binding.rvFolderCutter.adapter = folderAdapter
        binding.rvFolderCutter.setHasFixedSize(true)
        binding.rvFolderCutter.layoutManager = LinearLayoutManager(requireContext())

        audioCutterAdapter.registerAdapterDataObserver(adapterObserver)

        binding.rvAudioCutter.addOnScrollListener(object : RecyclerView.OnScrollListener() {      // su kien luu lai gia tri scroll
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                positionRv = binding.rvAudioCutter.computeVerticalScrollOffset()
            }
        })

    }

    /*  override fun showDialogSetAs(itemAudio: AudioCutterView) {
          audioCutterItem = itemAudio
          filePathAudio = itemAudio.audioFile.getFilePath()
          if (contactPermissionRequest.isPermissionGranted() && writeSettingPermissionRequest.isPermissionGranted()) {
              ManagerFactory.getAudioFileManager().init(requireContext())
              showDialogSetAsTypeAudio()
          } else {
              ContactPermissionDialog.newInstance {
                  resetRequestingPermission()
                  pendingRequestingPermission = CUT_CHOOSE_REQUESTING_PERMISSION
                  if (!contactPermissionRequest.isPermissionGranted()) {
                      contactPermissionRequest.requestPermission()
                  }
              }.show(
                  requireActivity().supportFragmentManager,
                  ContactPermissionDialog::class.java.name
              )
          }
      }*/

    override fun showDialogSetAs(itemAudio: AudioCutterViewItem) {
        audioCutterItem = itemAudio
        filePathAudio = itemAudio.audioFile.getFilePath()

        if (contactPermissionRequest.isPermissionGranted() && writeSettingPermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            showDialogSetAsTypeAudio()
        } else {
            ContactPermissionDialog.newInstance {
                resetRequestingPermission()
                pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
                checkPermissionRequest()
            }
                .show(requireActivity().supportFragmentManager, ContactPermissionDialog::class.java.name)
        }

    }

    private fun checkPermissionRequest() {
        if (!contactPermissionRequest.isPermissionGranted()) {
            contactPermissionRequest.requestPermission()
        } else if (!writeSettingPermissionRequest.isPermissionGranted()) {
            writeSettingPermissionRequest.requestPermission()
        }
    }

    private fun requestPermissinWriteSetting() {
        if (writeSettingPermissionRequest.isPermissionGranted()) {
            showDialogSetAsTypeAudio()
        } else {
            resetRequestingPermission()
            pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
            writeSettingPermissionRequest.requestPermission()
        }
    }

    private fun showDialogSetAsTypeAudio() {
        dialog.setOnCallBack(this)
        dialog.show(requireActivity().supportFragmentManager, "TAG")

    }

    override fun onCutItemClicked(itemAudio: AudioCutterViewItem) {
        audioCutterModel.stop()
        if (itemAudio.audioFile.duration < Constance.MIN_DURATION) {
            val dialogSnack = Snackbar.make(requireView(), getString(R.string.notification_file_was_short_mystudio_screen), Snackbar.LENGTH_SHORT)
            dialogSnack.show()
        } else {
            previousStatus()
            viewStateManager.onCuttingItemClicked(this, itemAudio)
        }
    }

    override fun onTickAudio(pos: Int) {
        indexChoose = pos
    }

    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }

    override fun setAsTypeAudio(typeAudioSetAs: TypeAudioSetAs) {
        var rs = false
        Log.d(TAG, "setAudioAs: ${audioCutterItem.audioFile.fileName}")
        when (typeAudioSetAs) {

            TypeAudioSetAs.RINGTONE -> {

                if (ManagerFactory.getRingtonManager().setRingTone(audioCutterItem.audioFile)) {
                    showNotification(getString(R.string.result_screen_set_ringtone_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_ringtone_fail))
                }
                dialog.dismiss()
            }
            TypeAudioSetAs.ALARM -> {
                if (ManagerFactory.getRingtonManager().setAlarmManager(audioCutterItem.audioFile)) {
                    showNotification(getString(R.string.result_screen_set_alarm_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_alarm_fail))
                }
                dialog.dismiss()
            }
            TypeAudioSetAs.NOTIFICATION -> {
                if (ManagerFactory.getRingtonManager()
                        .setNotificationSound(audioCutterItem.audioFile)) {
                    showNotification(getString(R.string.result_screen_set_notification_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_notification_fail))
                }
                dialog.dismiss()
            }
            else -> {
                filePathAudio?.let {
                    dialog.dismiss()
                    audioCutterModel.stop()
                    viewStateManager.onCutScreenSetRingtoneContact(this, filePathAudio!!)
                }
            }
        }
//        if (rs) {
//            dialog.dismiss()
//            dialogDone.show(childFragmentManager, SetAsDoneDialog::class.java.name)
//            showNotification(getString(R.string.my_studio_rename_audio_file_successfull))
//        }

    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivAudioCutterScreenFile -> {
                getAllFile()
            }
            binding.ivCutterScreenSearch -> {
                isSearchStatus = true
                searchAudiofile()
            }
            binding.ivCutterScreenBackEdt -> {
                previousStatus()
                isSearchStatus = false
            }
            binding.ivCutterScreenClose -> {
                clearText()
            }
            binding.ivCutterScreenBack -> {
                activity?.onBackPressed()
            }
            binding.ivCutterScreenSort -> {
                audioCutterModel.clickedOnSortButton()
            }
            binding.linear -> {
                audioCutterModel.showFolder()
            }
        }
    }


    private fun clearText() {
        if (binding.edtCutterSearch.text.toString().isNotEmpty()) {
            binding.edtCutterSearch.setText("")
        }
    }

    private fun previousStatus() {
        hideKeyboard()
        binding.rvAudioCutter.visibility = View.VISIBLE
//        binding.rvAudioCutter.scrollToPosition(indexChoose)
        binding.tvEmptyListCutter.visibility = View.INVISIBLE
        binding.ivEmptyListCutter.visibility = View.INVISIBLE
        binding.edtCutterSearch.setText("")
        hideOrShowEditText(View.INVISIBLE)
        hideOrShowView(View.VISIBLE)
    }

    private fun searchAudiofile() {
        hideOrShowEditText(View.VISIBLE)
        hideOrShowView(View.INVISIBLE)

        showKeyboard()
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
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select a File "), REQ_CODE_PICK_SOUNDFILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK && intent != null) {

            val path = FileUtils.getUriPath(requireContext(), intent.data!!)
            path?.let {
                val audio = ManagerFactory.getAudioFileManager().findAudioFile(it)
                audio?.let {
                    if (audio.duration > Constance.MIN_DURATION) {
                        viewStateManager.onCuttingItemClicked(this, AudioCutterViewItem(audio))
                    } else {
                        Snackbar.make(requireView(), getString(R.string.notification_file_was_short_mystudio_screen), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getDefaultAudioPlayer().stop()
        audioCutterAdapter.unregisterAdapterDataObserver(adapterObserver)
    }

    /*init {
        writeSettingPermissionRequest.init()
        contactPermissionRequest.init()
    }*/

}







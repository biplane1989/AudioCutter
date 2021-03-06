package com.example.audiocutter.functions.mystudio.screens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MyStudioFragmentBinding
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.functions.audiochooser.dialogs.TypeShare
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.functions.common.ContactPermissionDialog
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterAdapter
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterScreenCallback
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.permissions.AppPermission
import com.example.audiocutter.permissions.ContactItemPermissionRequest
import com.example.audiocutter.permissions.PermissionManager
import com.example.audiocutter.permissions.WriteSettingPermissionRequest
import com.example.audiocutter.util.REQUEST_CODE
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.requestPermission


class MyStudioScreen : BaseFragment(), AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener, CancelDialogListener, DialogAppShare.DialogAppListener {

    private lateinit var listUris: ArrayList<Uri>
    private lateinit var newList: List<String>
    private lateinit var dialogSetAs: SetAsDialog
    private lateinit var binding: MyStudioFragmentBinding
    private val TAG = "giangtd"
    private lateinit var myStudioViewModel: MyStudioViewModel
    private lateinit var audioCutterAdapter: AudioCutterAdapter
    private var typeAudio: Int = -1
    private var isDeleteClicked = true
    private var isShareClicked = true
    private var dialog: CancelDialog? = null
    private lateinit var audioFile: AudioFile
    private lateinit var dialogShare: DialogAppShare

    private var pendingRequestingPermission = 0
    private val WRITESETTING_ITEM_REQUESTING_PERMISSION = 1 shl 5

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

    private fun resetRequestingPermission() {
        pendingRequestingPermission = 0
    }

    override fun setMenuVisibility(menuVisible: Boolean) {      // su kien khi chuyen tab
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            // true
        } else {
            //fail
            if (this.isVisible) {
                myStudioViewModel.stopMediaPlayerWhenTabSelect()
            }
        }
    }

    private val listAudioObserver = Observer<List<AudioFileView>> { listAudio ->

        listAudio?.let {

            if (myStudioViewModel.isAllChecked()) {
//                cl_delete_all.visibility = View.GONE
            } else {
                binding.clDeleteAll.visibility = View.VISIBLE
                checkAllItemSelected()
            }

            if (!myStudioViewModel.isExitItemSelectDelete()) {
                binding.clDeleteAll.visibility = View.GONE
            }
            audioCutterAdapter.submitList(ArrayList(listAudio))

        }
    }

    // observer loading sstatus
    private val loadingStatusObserver = Observer<Boolean> {
        if (it) {
            binding.pbAudioCutter.visibility = View.VISIBLE
        } else {
            binding.pbAudioCutter.visibility = View.GONE
        }
    }

    // observer loading done danh cho dialog
    private val loadingDoneObserver = Observer<Boolean> {
        if (it) {
            isDeleteClicked = true
            dialog?.let {
                it.dismiss()
            }
        }
    }

    // observer is empty sstatus
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
//            binding.clDeleteAll.visibility = View.GONE
            binding.llNoFinishTask.visibility = View.VISIBLE
        } else {
            binding.llNoFinishTask.visibility = View.GONE
        }
    }

    private val actionObserver = Observer<ActionData> { it ->
        onReceivedAction(it.action, it.data)
    }

    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            binding.rvListAudioCutter.scrollToPosition(0)
        }
    }

    private fun onReceivedAction(action: String, type: Int) {
        if (action in arrayListOf(Constance.ACTION_CHECK_DELETE, Constance.ACTION_DELETE_ALL, Constance.ACTION_DELETE_STATUS)) if (type != typeAudio) {
            return
        }
        when (action) {
            Constance.ACTION_DELETE_STATUS -> { // trang thai isdelete
                myStudioViewModel.changeAutoItemToDelete()

//                if (myStudioViewModel.isAllChecked()) { // n???u kh??ng c??n data th?? s??? ko hi???n checkall
//                    binding.clDeleteAll.visibility = View.VISIBLE
//                } else {
//                    binding.clDeleteAll.visibility = View.GONE
//                }

                if (!myStudioViewModel.isExitItemSelectDelete()) {
                    binding.clDeleteAll.visibility = View.GONE
                } else {
                    binding.clDeleteAll.visibility = View.VISIBLE
                }
                Log.d("giangtd001", "onReceivedAction: ACTION_DELETE_STATUS")
            }
            Constance.ACTION_HIDE -> {  // trang thai undelete
                myStudioViewModel.changeAutoItemToMore()
                binding.clDeleteAll.visibility = View.GONE
                binding.ivCheck.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
            }
            Constance.ACTION_DELETE_ALL -> {
                runOnUI {
                    if (myStudioViewModel.deleteAllItemSelected(requireArguments().getInt(BUNDLE_NAME_KEY))) { // n???u delete th??nh c??ng th?? s??? hi???n dialog th??nh c??ng

                        showNotification(getString(R.string.my_studio_delete_successfull))
                    } else {

                        showNotification(getString(R.string.my_studio_delete_fail))
                    }
                    myStudioViewModel.changeAutoItemToMore()
                    binding.clDeleteAll.visibility = View.GONE

                }
            }
            Constance.ACTION_STOP_MUSIC -> {
                myStudioViewModel.stopMediaPlayerWhenTabSelect()
            }
            Constance.ACTION_CHECK_DELETE -> {
                if (!myStudioViewModel.isChecked()) {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_DELETE, Constance.FALSE)           // false
                } else {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_DELETE, Constance.TRUE)           // true
                }
            }

            Constance.ACTION_SHARE -> {
                if (requireArguments().getInt(BUNDLE_NAME_KEY) == type) {
                    if (!myStudioViewModel.isChecked()) {
//                        Toast.makeText(context, getString(R.string.my_studio_notification_chose_item_share), Toast.LENGTH_SHORT)
//                            .show()
                        showNotification(getString(R.string.my_studio_notification_chose_item_share))
                    } else {
                        if (isShareClicked) {
                            newList = myStudioViewModel.getListShare()
                            listUris = ArrayList<Uri>()
                            newList.forEach {
                                val uri = it.toUri()
                                listUris.add(uri)
                                Log.d(TAG, "shareMultiFileAudioToAppDevices: $it")
                            }
                            showDialogShareMultiFile()
                            Log.d(TAG, "onReceivedAction: list share size : " + newList.size)
//                            isShareClicked = false
                        }
                    }
                }
            }
        }
    }


    companion object {
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"

        @JvmStatic
        fun newInstance(typeAudio: Int): MyStudioScreen {
            val MyStudio = MyStudioScreen()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, typeAudio)
            MyStudio.arguments = bundle
            return MyStudio
        }
    }

    private fun init() {
        val linearLayoutManager = LinearLayoutManager(context)
        binding.rvListAudioCutter.layoutManager = linearLayoutManager
        binding.rvListAudioCutter.adapter = audioCutterAdapter
        audioCutterAdapter.registerAdapterDataObserver(adapterObserver)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        myStudioViewModel = ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this, myStudioViewModel.getAudioPlayer(), myStudioViewModel.getAudioEditorManager(), lifecycleScope)
        typeAudio = requireArguments().getInt(BUNDLE_NAME_KEY)  // l???y typeAudio c???a t???ng lo???i fragment

        myStudioViewModel.init(typeAudio)
        myStudioViewModel.getListAudioFile().observe(this, listAudioObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_fragment, container, false)

        PermissionManager.getAppPermission()
            .observe(this.viewLifecycleOwner, Observer<AppPermission> {
                if (contactPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and WRITESETTING_ITEM_REQUESTING_PERMISSION) != 0) {
                    resetRequestingPermission()
                    requestPermissinWriteSetting()
                    if (writeSettingPermissionRequest.isPermissionGranted() && (pendingRequestingPermission and WRITESETTING_ITEM_REQUESTING_PERMISSION) != 0) {
                        showDialogSetAs()
                    }
                }

            })
//            myStudioViewModel.getListAudioFile().observe(viewLifecycleOwner, listAudioObserver)
        myStudioViewModel.getLoadingStatus().observe(viewLifecycleOwner, loadingStatusObserver)

        myStudioViewModel.getIsEmptyStatus().observe(viewLifecycleOwner, isEmptyStatusObserver)

        myStudioViewModel.getLoadingDone().observe(viewLifecycleOwner, loadingDoneObserver)

        myStudioViewModel.getAction().observe(viewLifecycleOwner, actionObserver)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        binding.clDeleteAll.setOnClickListener(View.OnClickListener {
            myStudioViewModel.clickSelectAllBtn()
            checkAllItemSelected()
        })
    }

    override fun showMenu(view: View, audioFile: AudioFile) { // click item setting
        this.audioFile = audioFile
        val popup = android.widget.PopupMenu(context, view)
        popup.inflate(R.menu.output_audio_manager_screen_popup_menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            val dialogSnack = Snackbar.make(requireView(), getString(R.string.notification_file_was_short_mystudio_screen), Snackbar.LENGTH_SHORT)
            item?.let {
                when (item.itemId) {
                    R.id.set_as -> {
                        checkSetAsWriteSettingPermission()
                    }
                    R.id.cut -> {
                        Log.d(TAG, "showMenu: duration ${audioFile.duration}")
                        if (audioFile.duration < Constance.MIN_DURATION) {
                            dialogSnack.show()
                        } else {
                            viewStateManager.myStudioCuttingItemClicked(this, audioFile.file.absolutePath)
                        }
                    }
                    R.id.open_with -> {
                        audioFile.uri?.let {
                            Utils.openWithApp(requireContext(), it)
                        }

                        //open with screen
                    }
                    R.id.share -> {
                        showDialogShareFile()
                    }
                    R.id.rename -> {
                        val dialog = RenameDialog.newInstance(this, typeAudio, audioFile.file.absolutePath, audioFile.fileName)
                        dialog.show(childFragmentManager, RenameDialog.TAG)
                    }
                    R.id.info -> {
                        val dialog = InfoDialog.newInstance(audioFile.fileName, audioFile.file.absolutePath)
                        dialog.show(childFragmentManager, InfoDialog.TAG)
                    }
                    R.id.delete -> {
                        Log.d(TAG, "showMenu: DeleteDialog")
                        if (childFragmentManager.findFragmentByTag(DeleteDialog.TAG) == null) {
                            val dialog = DeleteDialog.newInstance(this, audioFile.file.absolutePath)
                            dialog.show(childFragmentManager, DeleteDialog.TAG)
                        } else {
                            // no thing
                        }
                    }
                    else -> {
                        // no thing
                    }
                }
            }

            true
        }
        popup.show()
    }

    private fun requestPermissinWriteSetting() {
        if (writeSettingPermissionRequest.isPermissionGranted()) {
            showDialogSetAs()
        } else {
            resetRequestingPermission()
            pendingRequestingPermission = WRITESETTING_ITEM_REQUESTING_PERMISSION
            writeSettingPermissionRequest.requestPermission()
        }
    }

    private fun checkSetAsWriteSettingPermission() {

        if (contactPermissionRequest.isPermissionGranted() && writeSettingPermissionRequest.isPermissionGranted()) {
            ManagerFactory.getAudioFileManager().init(requireContext())
            showDialogSetAs()
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

    private fun showDialogSetAs() {
        dialogSetAs = SetAsDialog.newInstance(this, audioFile.uri.toString())
        dialogSetAs.show(childFragmentManager, SetAsDialog.TAG)
    }

    private fun showDialogShareFile() {
        dialogShare = DialogAppShare(requireContext(), Utils.getListAppQueryReceiveOnlyData(requireContext()), TypeShare.ONLYFILE, false)
        dialogShare.setOnCallBack(this)
        dialogShare.show(requireActivity().supportFragmentManager, "TAG_DIALOG")
    }

    private fun checkAllItemSelected() {
        if (myStudioViewModel.isAllChecked()) {
            binding.ivCheck.setImageResource(R.drawable.my_studio_screen_icon_checked)
        } else {
            binding.ivCheck.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
        }
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun checkDeletePos(position: Int) {
        myStudioViewModel.checkItemPosition(position)
        checkAllItemSelected()
    }

    override fun isShowPlayingAudio(position: Int) {
        myStudioViewModel.showPlayingAudio(position)
    }

    override fun cancelLoading(id: Int) {      // cancel loading item
        if (isDeleteClicked) {
            dialog = CancelDialog.newInstance(this, id)
            dialog?.let {
                it.show(childFragmentManager, CancelDialog.TAG)
            }
            isDeleteClicked = false
        }
    }

    override fun errorConverting(fileName: String) {
        context?.let {
            showNotification(fileName + it.resources.getString(R.string.my_studio_screen_converting_error))
        }
    }

    // hanlder linterner on dialog rename
    override fun onRenameClick(newName: String, type: Int, filePath: String) {
        /**handle data rename change name to file insert to mediastore**/
        val typeFolder: Folder = when (type) {
            0 -> Folder.TYPE_CUTTER

            1 -> Folder.TYPE_MERGER

            else -> Folder.TYPE_MIXER
        }
        if (myStudioViewModel.renameAudio(newName, typeFolder, filePath)) {

            showNotification(getString(R.string.my_studio_rename_audio_file_successfull))
        } else {

            showNotification(getString(R.string.my_studio_rename_audio_file_fail))
        }

    }

    // hanlder linterner on dialog set as
    override fun onsetAsListenner(type: Int, uri: String) {
        when (type) {
            Constance.RINGTONE_TYPE -> {
                if (myStudioViewModel.setRingTone(uri)) {
                    showNotification(getString(R.string.result_screen_set_ringtone_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_ringtone_fail))
                }
            }
            Constance.ALARM_TYPE -> {
                if (myStudioViewModel.setAlarm(uri)) {
                    showNotification(getString(R.string.result_screen_set_alarm_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_alarm_fail))
                }
            }
            Constance.NOTIFICATION_TYPE -> {
                if (myStudioViewModel.setNotification(uri)) {
                    showNotification(getString(R.string.result_screen_set_notification_successful))
                } else {
                    showNotification(getString(R.string.result_screen_set_notification_fail))
                }
            }
        }
    }

    override fun setRingtoneForContact() {
        dialogSetAs.dismiss()
        viewStateManager.myStudioSetContactItemClicked(this, audioFile.file.absolutePath)
    }

    // click delete button on dialog delete
    override fun onDeleteClick(pathFolder: String) {
        runOnUI {
            if (myStudioViewModel.deleteItem(pathFolder, requireArguments().getInt(BUNDLE_NAME_KEY))) { // n???u delete th??nh c??ng th?? s??? hi???n dialog th??nh c??ng

                showNotification(getString(R.string.my_studio_delete_successfull))
            } else {

                showNotification(getString(R.string.my_studio_delete_fail))
            }
        }
    }

    // click cancel button on dialog delete
    override fun onCancel() {
    }

    // cancel dialog
    override fun onDestroyView() {
        super.onDestroyView()
        myStudioViewModel.stopMediaPlayerWhenTabSelect()
        audioCutterAdapter.unregisterAdapterDataObserver(adapterObserver)
    }

    override fun onCancelDeleteClick(id: Int) {        // cancel dialog
        myStudioViewModel.cancelLoading(id)
        isDeleteClicked = true

        showNotification(getString(R.string.my_studio_delete_successfull))
    }

    override fun onCancelDialog() {
        isDeleteClicked = true
    }

    override fun shareFileAudioToAppDevices(typeShare: TypeShare) {
        dialogShare.dismiss()
        when (typeShare) {
            TypeShare.ONLYFILE -> {
                audioFile.uri?.let {

                    Utils.shareFileAudio(requireContext(), it, null)
                }
            }
            TypeShare.MULTIFILE -> {
                Utils.shareMutilpleFile(requireContext(), listUris, null)
            }
        }

    }

    override fun shareFilesToAppsDialog(pkgName: String, typeShare: TypeShare, isDialogMulti: Boolean?) {
        if (typeShare == TypeShare.ONLYFILE) {
            isDialogMulti?.let {
                if (isDialogMulti) {
                    Utils.shareFileAudio(requireContext(), listUris[0], pkgName)
                } else {
                    audioFile.uri?.let {
                        Utils.shareFileAudio(requireContext(), it, pkgName)
                    }
                }
            }
        } else {
            Utils.shareMutilpleFile(requireContext(), listUris, pkgName)
        }

    }

    private fun showDialogShareMultiFile() {
        val listApps: List<ItemAppShare>
        if (listUris.size > 1) {
            listApps = Utils.getListAppQueryReceiveMutilData(requireContext())
            dialogShare = DialogAppShare(requireContext(), listApps, TypeShare.MULTIFILE, true)

        } else {
            listApps = Utils.getListAppQueryReceiveOnlyData(requireContext())
            dialogShare = DialogAppShare(requireContext(), listApps, TypeShare.ONLYFILE, true)
        }
        dialogShare.setOnCallBack(this)
        dialogShare.show(childFragmentManager, DialogAppShare::class.java.name)
    }


    //ToDo ("fragment nao  su dung thi override")
    override fun getFragmentViewModel(): IViewModel? {
        return myStudioViewModel
    }

    init {
        writeSettingPermissionRequest.init()
        contactPermissionRequest.init()
    }
}
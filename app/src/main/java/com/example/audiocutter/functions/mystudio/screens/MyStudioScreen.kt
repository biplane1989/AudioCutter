package com.example.audiocutter.functions.mystudio.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MyStudioFragmentBinding
import com.example.audiocutter.functions.audiochooser.dialogs.DialogAppShare
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterAdapter
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterScreenCallback
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyStudioScreen() : BaseFragment(), AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener, CancelDialogListener, DialogAppShare.DialogAppListener {

    private lateinit var binding: MyStudioFragmentBinding
    private val TAG = "giangtd"
    private lateinit var myStudioViewModel: MyStudioViewModel
    private lateinit var audioCutterAdapter: AudioCutterAdapter
    private var typeAudio: Int = -1
    private var isDeleteClicked = true
    private var dialog: CancelDialog? = null
    private lateinit var audioFile: AudioFile
    private lateinit var dialogShare: DialogAppShare
    private val linearLayoutManager = LinearLayoutManager(context)


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
        if (it && dialog != null) {
            dialog!!.dismiss()
        }
    }

    // observer is empty sstatus
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
            binding.clDeleteAll.visibility = View.GONE
            binding.llNoFinishTask.visibility = View.VISIBLE
        } else {
            binding.llNoFinishTask.visibility = View.GONE
        }
    }

    private val actionObserver = Observer<ActionData> { it ->
        onReceivedAction(it.action, it.data as Int)
    }

    private fun onReceivedAction(action: String, type: Int) {
        if (action in arrayListOf(Constance.ACTION_CHECK_DELETE, Constance.ACTION_DELETE_ALL)) if (type != (typeAudio as Int)) {
            return
        }
        when (action) {
            Constance.ACTION_UNCHECK -> { // trang thai isdelete
                myStudioViewModel.changeAutoItemToDelete()
                if (myStudioViewModel.isAllChecked()) { // nếu không còn data thì sẽ ko hiện checkall
                    binding.clDeleteAll.visibility = View.GONE
                } else {
                    binding.clDeleteAll.visibility = View.VISIBLE
                }
            }
            Constance.ACTION_HIDE -> {  // trang thai undelete
//                audioCutterAdapter.submitList(myStudioViewModel.changeAutoItemToMore())
                myStudioViewModel.changeAutoItemToMore()
                binding.clDeleteAll.visibility = View.GONE
                binding.ivCheck.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
            }
            Constance.ACTION_DELETE_ALL -> {
                if (myStudioViewModel.isAllChecked()) {   // check nếu tất cả đã xóa thì ẩn nút selectall
                    binding.clDeleteAll.visibility = View.GONE
                }
                runOnUI {
                    if (myStudioViewModel.deleteAllItemSelected(requireArguments().getInt(BUNDLE_NAME_KEY))) { // nếu delete thành công thì sẽ hiện dialog thành công

                        val mySnackbar = Snackbar.make(requireView(), getString(R.string.my_studio_delete_successfull), Snackbar.LENGTH_LONG)
                        mySnackbar.show()
                    } else {
                        val mySnackbar = Snackbar.make(requireView(), getString(R.string.my_studio_delete_fail), Snackbar.LENGTH_LONG)
                        mySnackbar.show()
                    }
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
        linearLayoutManager.reverseLayout = true        // set positiion bottom item
        binding.rvListAudioCutter.layoutManager = linearLayoutManager

        binding.rvListAudioCutter.adapter = audioCutterAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myStudioViewModel = ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this, myStudioViewModel.getAudioPlayer(), myStudioViewModel.getAudioEditorManager(), lifecycleScope)
        typeAudio = requireArguments().getInt(BUNDLE_NAME_KEY)  // lấy typeAudio của từng loại fragment

        myStudioViewModel.init(typeAudio)
        myStudioViewModel.getListAudioFile().observe(this, listAudioObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_fragment, container, false)
        runOnUI {
//            myStudioViewModel.getListAudioFile().observe(viewLifecycleOwner, listAudioObserver)

            myStudioViewModel.getLoadingStatus().observe(viewLifecycleOwner, loadingStatusObserver)

            myStudioViewModel.getIsEmptyStatus().observe(viewLifecycleOwner, isEmptyStatusObserver)

            myStudioViewModel.getLoadingDone().observe(viewLifecycleOwner, loadingDoneObserver)

            myStudioViewModel.getAction().observe(viewLifecycleOwner, actionObserver)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.clDeleteAll.setOnClickListener(View.OnClickListener {
            audioCutterAdapter.submitList(myStudioViewModel.clickSelectAllBtn())
            checkAllItemSelected()
        })
    }

    override fun showMenu(view: View, audioFile: AudioFile) { // click item setting
        val popup = android.widget.PopupMenu(context, view)
        popup.inflate(R.menu.output_audio_manager_screen_popup_menu)
        popup.setOnMenuItemClickListener(android.widget.PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.set_as -> {
                    val dialog = SetAsDialog.newInstance(this, audioFile.uri.toString())
                    dialog.show(childFragmentManager, SetAsDialog.TAG)
                }
                R.id.cut -> {
                    viewStateManager.myStudioCuttingItemClicked(this, audioFile.file.absolutePath)
                }
                R.id.contacs -> {
                    // contacs screen
                    viewStateManager.myStudioSetContactItemClicked(this, audioFile.file.absolutePath)
                }
                R.id.open_with -> {
                    audioFile.uri?.let {
                        Utils.openWithApp(requireContext(), it)
                    }

                    //open with screen
                }
                R.id.share -> {
                    this.audioFile = audioFile

                    ShowDialogShareFile()
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
                    if (childFragmentManager.findFragmentByTag(DeleteDialog.TAG) == null) {
                        val dialog = DeleteDialog.newInstance(this, audioFile.file.absolutePath)
                        dialog.show(childFragmentManager, DeleteDialog.TAG)
                    }
                }
            }

            true
        })
        popup.show()
    }

    private fun ShowDialogShareFile() {
        dialogShare = DialogAppShare(requireContext())
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

    override fun checkDeletePos(position: Int) {
//        audioCutterAdapter.submitList(myStudioViewModel.checkItemPosition(position))
        myStudioViewModel.checkItemPosition(position)
        checkAllItemSelected()
    }

    override fun isShowPlayingAudio(positition: Int, heightItem: Float) {
        myStudioViewModel.showPlayingAudio(positition)

        Log.d(TAG, "isShowPlayingAudio: ${linearLayoutManager.findLastVisibleItemPosition()}")
        Log.d(TAG, "isShowPlayingAudio: ${linearLayoutManager.findLastCompletelyVisibleItemPosition()}")

        if (linearLayoutManager.findLastVisibleItemPosition() == positition) {
//            linearLayoutManager.scrollToPositionWithOffset(positition, Utils.dpToPx(requireContext(), heightItem)
//                .toInt() + Utils.dpToPx(requireContext(), 34f).toInt()+1500)

            linearLayoutManager.scrollToPositionWithOffset(positition, 1500)
        }
    }

    override fun cancelLoading(id: Int) {      // cancel loading item
        if (isDeleteClicked) {
            dialog = CancelDialog.newInstance(this, id)
            dialog!!.show(childFragmentManager, CancelDialog.TAG)
            isDeleteClicked = false
        }
    }

    override fun errorConverting(fileName: String) {
        val mySnackbar = Snackbar.make(requireView(), fileName + getString(R.string.my_studio_screen_converting_error), Snackbar.LENGTH_LONG)
        mySnackbar.show()
    }

    // hanlder linterner on dialog rename
    override fun onRenameClick(newName: String, type: Int, filePath: String) {
        /**handle data rename change name to file insert to mediastore**/
        val typeFolder: Folder = when (type) {
            0 -> Folder.TYPE_CUTTER

            1 -> Folder.TYPE_MERGER

            else -> Folder.TYPE_MIXER
        }
        myStudioViewModel.renameAudio(newName, typeFolder, filePath)


    }

    // hanlder linterner on dialog set as
    override fun onsetAsListenner(type: Int, uri: String) {
        when (type) {
            Constance.RINGTONE_TYPE -> {
                if (myStudioViewModel.setRingTone(uri)) {

                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_ringtone_successful), Snackbar.LENGTH_LONG)
                    mySnackbar.show()
                } else {
                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_ringtone_fail), Snackbar.LENGTH_LONG)
                    mySnackbar.show()
                }
            }
            Constance.ALARM_TYPE -> {
                if (myStudioViewModel.setAlarm(uri)) {
                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_alarm_successful), Snackbar.LENGTH_LONG)
                    mySnackbar.show()

                } else {
                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_alarm_fail), Snackbar.LENGTH_LONG)
                    mySnackbar.show()

                }
            }
            Constance.NOTIFICATION_TYPE -> {
                if (myStudioViewModel.setNotification(uri)) {
                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_notification_successful), Snackbar.LENGTH_LONG)
                    mySnackbar.show()
                } else {
                    val mySnackbar = Snackbar.make(requireView(), getString(R.string.result_screen_set_notification_fail), Snackbar.LENGTH_LONG)
                    mySnackbar.show()
                }
            }
        }
    }

    // click delete button on dialog delete
    override fun onDeleteClick(pathFolder: String) {
        runOnUI {
            if (myStudioViewModel.deleteItem(pathFolder, requireArguments().getInt(BUNDLE_NAME_KEY))) { // nếu delete thành công thì sẽ hiện dialog thành công
                val mySnackbar = Snackbar.make(requireView(), getString(R.string.my_studio_delete_successfull), Snackbar.LENGTH_LONG)
                mySnackbar.show()
            } else {
                val mySnackbar = Snackbar.make(requireView(), getString(R.string.my_studio_delete_fail), Snackbar.LENGTH_LONG)
                mySnackbar.show()

            }
        }
    }

    // click cancel button on dialog delete
    override fun onCancel() {
    }

    override fun onCancelDeleteClick(id: Int) {        // cancel dialog
        myStudioViewModel.cancelLoading(id)
        isDeleteClicked = true

        val mySnackbar = Snackbar.make(requireView(), getString(R.string.my_studio_delete_successfull), Snackbar.LENGTH_LONG)
        mySnackbar.show()

    }

    override fun onCancelDialog() {
        isDeleteClicked = true
    }


    override fun shareFileAudioToAppDevices() {
        dialogShare.dismiss()
        Utils.shareFileAudio(requireContext(), audioFile)
    }

    override fun shareFilesToAppsDialog(pkgName: String) {
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, audioFile.uri)
        intent.type = "audio/*"
        intent.`package` = pkgName
        intent.action = Intent.ACTION_SEND
        requireActivity().startActivity(intent)
    }

    //ToDo ("fragment nao  su dung thi override")
    override fun getFragmentViewModel(): IViewModel? {
        return myStudioViewModel
    }
}
package com.example.audiocutter.functions.mystudio.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MyStudioFragmentBinding
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterAdapter
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterScreenCallback
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_fragment.*


class MyStudioScreen() : BaseFragment(), AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener, CancelDialogListener {

    private lateinit var binding: MyStudioFragmentBinding
    private val TAG = "giangtd"
    private lateinit var myStudioViewModel: MyStudioViewModel
    private lateinit var audioCutterAdapter: AudioCutterAdapter
    private var typeAudio: Int = -1
    private var isDoubleDeleteClicked = true
    private var isDeleteClicked = true
    private var dialog: CancelDialog? = null

    val listAudioObserver = Observer<List<AudioFileView>> { listAudio ->

        Log.d(TAG, "listAudioObserver size : " + listAudio.size)
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
            cl_delete_all.visibility = View.GONE
            binding.llNoFinishTask.visibility = View.VISIBLE
        } else {
            binding.llNoFinishTask.visibility = View.GONE
        }
    }

    companion object {
        //        val TAG = "FragmentMyStudio"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"

        @JvmStatic
        fun newInstance(typeAudio: Int): MyStudioScreen {
            val MyStudio = MyStudioScreen()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, typeAudio)
            MyStudio.arguments = bundle
            return MyStudio
        }

//        @JvmStatic
//        fun buildArgs(typeAudio: Int): Bundle {
//            val bundle = Bundle()
//            bundle.putInt(BUNDLE_NAME_KEY, typeAudio)
//            return bundle
//        }
    }

    fun init() {
        binding.rvListAudioCutter.layoutManager = LinearLayoutManager(context)
        binding.rvListAudioCutter.adapter = audioCutterAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myStudioViewModel = ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)
        typeAudio = requireArguments().getInt(BUNDLE_NAME_KEY)  // lấy typeAudio của từng loại fragment
        myStudioViewModel.init(typeAudio)

//        runOnUI {
//        myStudioViewModel.getListAudioFile().observe(this as LifecycleOwner, listAudioObserver)
//        }
        Log.d(TAG, "onPostCreate: override myStudio")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_fragment, container, false)
        runOnUI {
            myStudioViewModel.getListAudioFile().observe(viewLifecycleOwner, listAudioObserver)

            myStudioViewModel.getLoadingStatus().observe(viewLifecycleOwner, loadingStatusObserver)

            myStudioViewModel.getIsEmptyStatus().observe(viewLifecycleOwner, isEmptyStatusObserver)

            myStudioViewModel.getLoadingDone().observe(viewLifecycleOwner, loadingDoneObserver)
        }
        Log.d(TAG, "onCreateView: override myStudio")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        binding.ivCheck.setOnClickListener(View.OnClickListener {
            audioCutterAdapter.submitList(myStudioViewModel.clickSelectAllBtn())
            checkAllItemSelected()
        })
    }

    override fun play(position: Int) {
        myStudioViewModel.playingAudioAndchangeStatus(position)
    }

    override fun pause(position: Int) {
        myStudioViewModel.pauseAudioAndChangeStatus(position)
    }

    override fun resume(position: Int) {
        myStudioViewModel.resumeAudioAndChangeStatus(position)
    }

    override fun stop(position: Int) {
        myStudioViewModel.stopAudioAndChangeStatus(position)
    }

    override fun seekTo(cusorPos: Int) {
        myStudioViewModel.seekToAudio(cusorPos)
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
                    //cut screen
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.contacs -> {
                    // contacs screen
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.open_with -> {
                    //open with screen
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.share -> {
                    ManagerFactory.getAudioFileManager().shareFileAudio(audioFile)
                }
                R.id.rename -> {
                    val dialog = RenameDialog.newInstance(this, "giang")
                    dialog.show(childFragmentManager, RenameDialog.TAG)
                }
                R.id.info -> {
                    val dialog = InfoDialog.newInstance(audioFile.fileName, audioFile.file.absolutePath)
                    dialog.show(childFragmentManager, InfoDialog.TAG)
                }
                R.id.delete -> {
                    if (isDoubleDeleteClicked) {
                        val dialog = DeleteDialog.newInstance(this, audioFile.file.absolutePath)
                        dialog.show(childFragmentManager, DeleteDialog.TAG)
                        isDoubleDeleteClicked = false
                    }
                }
            }

            true
        })
        popup.show()
    }

    private fun checkAllItemSelected() {
        Log.d(TAG, "override checkAllItemSelected: ")
        if (myStudioViewModel.isAllChecked()) {
            iv_check.setImageResource(R.drawable.my_studio_screen_icon_checked)
        } else {
            iv_check.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
        }
    }

    override fun checkDeletePos(position: Int) {
        audioCutterAdapter.submitList(myStudioViewModel.checkItemPosition(position))
        checkAllItemSelected()
    }

    override fun isShowPlayingAudio(positition: Int) {
        audioCutterAdapter.submitList(myStudioViewModel.showPlayingAudio(positition))
    }

    override fun cancelLoading(id: Int) {      // cancel loading item
        Log.d(TAG, "cancelLoading: ")
        if (isDeleteClicked) {
            dialog = CancelDialog.newInstance(this, id)
            dialog!!.show(childFragmentManager, CancelDialog.TAG)
            isDeleteClicked = false
        }
    }

    // hanlder linterner on dialog rename
    override fun onRenameClick() {
        Log.d(TAG, "onRenameClick: rename")
    }

    // hanlder linterner on dialog set as
    override fun onsetAsListenner(type: Int, uri: String) {
        when (type) {
            Constance.RINGTONE_TYPE -> {
                if (myStudioViewModel.setRingTone(uri)) {
                    Toast.makeText(requireContext(), "Set Ringtone Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Ringtone Fail !", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            Constance.ALARM_TYPE -> {
                if (myStudioViewModel.setAlarm(uri)) {
                    Toast.makeText(requireContext(), "Set Alarm Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Alarm Fail !", Toast.LENGTH_SHORT).show()
                }
            }
            Constance.NOTIFICATION_TYPE -> {
                if (myStudioViewModel.setNotification(uri)) {
                    Toast.makeText(requireContext(), "Set Notification Successful !", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Set Notification Fail !", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // click delete button on dialog delete
    override fun onDeleteClick(pathFolder: String) {
        Log.d(TAG, "onDeleteClick: ")
        runOnUI {
            if (myStudioViewModel.deleteItem(pathFolder, requireArguments().getInt(BUNDLE_NAME_KEY))) { // nếu delete thành công thì sẽ hiện dialog thành công
                val dialog = DeleteSuccessfullyDialog()
                dialog.show(childFragmentManager, DeleteSuccessfullyDialog.TAG)
            } else {
                Toast.makeText(context, getString(R.string.my_studio_delete_fail), Toast.LENGTH_SHORT)
                    .show()
            }
            Log.d(TAG, "onReceivedAction: " + myStudioViewModel.deleteAllItemSelected(requireArguments().getInt(BUNDLE_NAME_KEY)))
        }
        isDoubleDeleteClicked = true
    }

    // click cancel button on dialog delete
    override fun onCancel() {
        isDoubleDeleteClicked = true
    }

    // nhận listernner từ fragment khác truyền đến
    override fun onReceivedAction(fragmentMeta: FragmentMeta) {
        // nếu typeAudio không bằng data của fragment thoát
        // ap dung 2 kieu truyen du lieu. 1 qua newIntent fragment, 2. truyen qua call back sendFragment
        if (fragmentMeta.action in arrayListOf(Constance.ACTION_CHECK_DELETE, Constance.ACTION_DELETE_ALL)) if (typeAudio != (fragmentMeta.data as Int)) {
            return
        }
        when (fragmentMeta.action) {
            Constance.ACTION_UNCHECK -> { // trang thai isdelete
                Log.d(TAG, "override onReceivedAction: ACTION_UNCHECK")
                myStudioViewModel.changeAutoItemToDelete()
                if (myStudioViewModel.isAllChecked()) { // nếu không còn data thì sẽ ko hiện checkall
                    cl_delete_all.visibility = View.GONE
                } else {
                    cl_delete_all.visibility = View.VISIBLE
                }
            }
            Constance.ACTION_HIDE -> {  // trang thai undelete
                audioCutterAdapter.submitList(myStudioViewModel.changeAutoItemToMore())
                cl_delete_all.visibility = View.GONE
                iv_check.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
            }
            Constance.ACTION_DELETE_ALL -> {
                if (myStudioViewModel.isAllChecked()) {   // check nếu tất cả đã xóa thì ẩn nút selectall
                    cl_delete_all.visibility = View.GONE
                }
                runOnUI {
                    if (myStudioViewModel.deleteAllItemSelected(requireArguments().getInt(BUNDLE_NAME_KEY))) { // nếu delete thành công thì sẽ hiện dialog thành công
                        val dialog = DeleteSuccessfullyDialog()
                        dialog.show(childFragmentManager, DeleteSuccessfullyDialog.TAG)
                    } else {
                        Toast.makeText(context, getString(R.string.my_studio_delete_fail), Toast.LENGTH_SHORT)
                            .show()
                    }
                    Log.d(TAG, "onReceivedAction: " + myStudioViewModel.deleteAllItemSelected(requireArguments().getInt(BUNDLE_NAME_KEY)))
                }
            }
            Constance.ACTION_STOP_MUSIC -> {
                myStudioViewModel.stopMediaPlayerWhenTabSelect()
            }
            Constance.ACTION_CHECK_DELETE -> {
                if (!myStudioViewModel.isChecked()) {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_DELETE, false)
                } else {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_DELETE, true)
                }
            }
        }
    }

    override fun onCancelDeleteClick(id: Int) {        // cancel dialog
        myStudioViewModel.cancelLoading(id)
        isDeleteClicked = true
    }

    override fun onCancelDialog() {
        isDeleteClicked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, " override onDestroyView: myStudio")
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        Log.d(TAG, "override onPostDestroy: myStudio")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "override onDetach: myStudio")
    }
}
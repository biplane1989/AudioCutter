package com.example.audiocutter.functions.mystudioscreen.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.mystudioscreen.AudioFileView
import com.example.audiocutter.functions.mystudioscreen.Constance
import com.example.audiocutter.functions.mystudioscreen.MyAudioManagerScreen
import com.example.audiocutter.functions.mystudioscreen.ShareFragment
import com.example.audiocutter.functions.mystudioscreen.dialog.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_fragment.*


class MyStudioFragment() : BaseFragment(), AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener {

    val TAG = "giangtd"
    lateinit var myStudioViewModel: MyStudioViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    var typeAudio: Int = -1
    var isDoubleDeleteClicked = true
    var isLoading = false   // trang thai load cua progressbar

    var currentView: View? = null

    // observer data
    val listAudioObserver = Observer<List<AudioFileView>> { listMusic ->
        if (listMusic.size == 0) {
            ll_no_finish_task.visibility = View.VISIBLE
            cl_delete_all.visibility = View.GONE
        } else {
            ll_no_finish_task.visibility = View.GONE
        }
        audioCutterAdapter.submitList(ArrayList(listMusic))

    }

    // observer playInfo mediaplayer
    private val playerInfoObserver = Observer<PlayerInfo> {
        if (myStudioViewModel.isPlayingStatus) {
            audioCutterAdapter.submitList(myStudioViewModel.updatePlayerInfo(it))
        }

    }

    companion object {
        val TAG = "FragmentMyStudio"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"

        @JvmStatic
        fun newInstance(typeAudio: Int): MyStudioFragment {
            val MyStudio = MyStudioFragment()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, typeAudio)
            MyStudio.arguments = bundle
            return MyStudio
        }
    }

    fun init() {
        rv_list_audio_cutter.layoutManager = LinearLayoutManager(context)
        rv_list_audio_cutter.setHasFixedSize(true)
        rv_list_audio_cutter.adapter = audioCutterAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        Log.d("taih", "onPostCreate ${this}")
        super.onPostCreate(savedInstanceState)
        myStudioViewModel = ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)

        isLoading = true
        runOnUI {
            val listAudioViewLiveData = myStudioViewModel.getData(typeAudio) // get data from funtion newIntance
            listAudioViewLiveData.observe(this as LifecycleOwner, listAudioObserver)
            isLoading = false

            currentView?.findViewById<ProgressBar>(R.id.pb_audio_cutter)?.visibility = View.GONE    // tai day hamonCreateView da chay xong r do runOnUI
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        typeAudio = requireArguments().getInt(BUNDLE_NAME_KEY)  // lấy typeAudio của từng loại fragment
        currentView = inflater.inflate(R.layout.my_studio_fragment, container, false)
        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (isLoading) {
            pb_audio_cutter.visibility = View.VISIBLE
        } else {
            pb_audio_cutter.visibility = View.GONE
        }
        iv_check.setOnClickListener(View.OnClickListener {
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
                    val dialog = SetAsDialog.newInstance(this, "giang")
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
                    val shareFragment = ShareFragment()
                    activity?.supportFragmentManager?.let {
                        shareFragment.show(it, shareFragment.getTag())
                    }
                }
                R.id.rename -> {
                    val dialog = RenameDialog.newInstance(this, "giang")
                    dialog.show(childFragmentManager, RenameDialog.TAG)
                }
                R.id.info -> {
                    val dialog = InfoDialog.newInstance(audioFile)
                    dialog.show(childFragmentManager, InfoDialog.TAG)
                }
                R.id.delete -> {
                    if (isDoubleDeleteClicked) {
                        val dialog = DeleteDialog.newInstance(this)
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

    // hanlder linterner on dialog rename
    override fun onRenameClick() {
        Log.d(TAG, "onRenameClick: rename")
    }

    // hanlder linterner on dialog set as
    override fun onsetAsListenner(type: Int) {
        when (type) {
            Constance.RINGTONE_TYPE -> {
                Log.d(TAG, "onsetAsListenner: ringtone")
            }
            Constance.ALARM_TYPE -> {
                Log.d(TAG, "onsetAsListenner: alarm")
            }
            Constance.NOTIFICATION_TYPE -> {
                Log.d(TAG, "onsetAsListenner: notification")
            }
        }
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        Log.d("taih", "onPostDestroy ${this}")
    }

    // click delete button on dialog delete
    override fun onDeleteClick() {
        Log.d(TAG, "onDeleteClick: ")

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
                audioCutterAdapter.submitList(myStudioViewModel.changeAutoItemToDelete())
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
                if (myStudioViewModel.isPlayingStatus) {
                    myStudioViewModel.stopMediaPlayerWhenTabSelect()
                }
            }

            Constance.ACTION_CHECK_DELETE -> {
                if (!myStudioViewModel.isChecked()) {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_CHECK_DELETE, false)
                } else {
                    sendFragmentAction(MyAudioManagerScreen::class.java.name, Constance.ACTION_CHECK_DELETE, true)
                }
            }
        }
    }

}
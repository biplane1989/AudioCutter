package com.example.audiocutter.functions.mystudio.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.mystudio.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.ShareFragment
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.fragment_audio_cutter.*

class MyStudioFragment() : BaseFragment(),
    AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener {

    val TAG = "giangtd"
    lateinit var myStudioViewModel: MyStudioViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    var isDoubleDeleteClicked = true

    // observer data
    val listAudioObserver = Observer<List<AudioFileView>> { listMusic ->
        if (listMusic.size == 0) {
            ll_no_finish_task.visibility = View.VISIBLE
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
        var typeAudio = -1
        val TAG = "FragmentMyStudio"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"

        @JvmStatic
        fun newInstance(typeAudio: Int): MyStudioFragment {
            val MyStudio = MyStudioFragment()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, typeAudio)
            MyStudio.arguments = bundle
            this.typeAudio = typeAudio
            Log.d(TAG, "newInstance: type audio : " + this.typeAudio)
            return MyStudio
        }

    }

    fun init() {
        rv_list_audio_cutter.layoutManager = LinearLayoutManager(context)
        rv_list_audio_cutter.setHasFixedSize(true)
        rv_list_audio_cutter.adapter = audioCutterAdapter
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myStudioViewModel =
            ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_cutter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        runOnUI {
            // nếu đã đăng ký observer thì phải remove
            pb_audio_cutter.visibility = View.VISIBLE
            val listAudioViewLiveData = myStudioViewModel.getData(typeAudio)
            listAudioViewLiveData.removeObserver(listAudioObserver)
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
            pb_audio_cutter.visibility = View.GONE

        }

        iv_check.setOnClickListener(View.OnClickListener {
            audioCutterAdapter.submitList(myStudioViewModel.clickSelectAllBtn())
            checkAllItemSelected()
        })
    }

    // nhận listernner từ fragment khác truyền đến
    override fun onReceivedAction(fragmentMeta: FragmentMeta) {

        when (fragmentMeta.action) {
            // trang thai isdelete
            Constance.ACTION_UNCHECK -> {
                audioCutterAdapter.submitList(myStudioViewModel.changeAutoItemToDelete())
                cl_delete_all.visibility = View.VISIBLE

            }
            // trang thai undelete
            Constance.ACTION_HIDE -> {
                audioCutterAdapter.submitList(myStudioViewModel.changeAutoItemToMore())
                cl_delete_all.visibility = View.GONE
                iv_check.setImageResource(R.drawable.my_studio_screen_icon_uncheck)
            }
            Constance.ACTION_DELETE_ALL -> {
                // check nếu tất cả đã xóa thì ẩn nút selectall
                if (myStudioViewModel.isAllChecked()) {
                    cl_delete_all.visibility = View.GONE
                }
                myStudioViewModel.deleteAllItemSelected()
            }
            Constance.ACTION_STOP_MUSIC -> {
                if (myStudioViewModel.isPlayingStatus) {
                    myStudioViewModel.stopMediaPlayerWhenTabSelect()
                }
            }
        }
    }

    override fun play(position: Int) {
        audioCutterAdapter.submitList(myStudioViewModel.playingAudioAndchangeStatus(position))
    }

    override fun pause(position: Int) {
        audioCutterAdapter.submitList(myStudioViewModel.pauseAudioAndChangeStatus(position))
    }

    override fun resume(position: Int) {

        audioCutterAdapter.submitList(myStudioViewModel.resumeAudioAndChangeStatus(position))
    }

    override fun stop(position: Int) {
        audioCutterAdapter.submitList(myStudioViewModel.stopAudioAndChangeStatus(position))
    }

    override fun seekTo(cusorPos: Int) {
        myStudioViewModel.seekToAudio(cusorPos)
    }

    // click item setting
    override fun showMenu(view: View, audioFile: AudioFile) {
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
                        shareFragment.show(
                            it, shareFragment.getTag()
                        )
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
                        val dialog = DeleteDialog.newInstance(this, "giang")
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

    // click delete button on dialog delete
    override fun onDeleteClick() {
        Log.d(TAG, "onDeleteClick: ")

        isDoubleDeleteClicked = true
    }

    // click cancel button on dialog delete
    override fun onCancel() {
        isDoubleDeleteClicked = true
    }

}
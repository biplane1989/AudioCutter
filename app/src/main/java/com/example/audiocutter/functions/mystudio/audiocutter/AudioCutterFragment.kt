package com.example.audiocutter.functions.mystudio.audiocutter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.functions.mystudio.ShareFragment
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.fragment_audio_cutter.*

class AudioCutterFragment() : BaseFragment(),
    AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener {

    val TAG = "giangtd"
    lateinit var audioCutterViewModel: AudioCutterViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    var checkedAllStatus = false
    var isDoubleDeleteClicked = true
    var sizeListDeleteAll = 0
    var listItemDelete = ArrayList<Int>()

    var deleteItemStatus: MutableLiveData<DeleteState> = MutableLiveData()

    private val deleteItemObserver = Observer<DeleteState> {
        audioCutterAdapter.updateDeleteStatus(it)
        when (it) {
            DeleteState.HIDE -> {
                cl_delete_all.visibility = View.GONE
                checkedAllStatus = false
            }
            DeleteState.UNCHECK -> {
                cl_delete_all.visibility = View.VISIBLE
                iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
            }
            DeleteState.CHECKED -> {
                iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
                cl_delete_all.visibility = View.VISIBLE
            }

        }
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioCutterAdapter.updateMedia(it)
    }

    companion object {
        fun newInstance(): AudioCutterFragment =
            AudioCutterFragment()
    }

    fun init() {
        rv_list_audio_cutter.layoutManager = LinearLayoutManager(context)
        rv_list_audio_cutter.setHasFixedSize(true)
        rv_list_audio_cutter.adapter = audioCutterAdapter

    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        audioCutterViewModel =
            ViewModelProviders.of(this).get(AudioCutterViewModel()::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)

        audioCutterViewModel.getListMusic()?.observe(this, Observer { listMusic ->
            listMusic?.let {
                sizeListDeleteAll = listMusic.size
                audioCutterAdapter.submitList(ArrayList(listMusic))
            }
        })
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
        deleteItemStatus.observe(this, deleteItemObserver)

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
            // load data
            pb_audio_cutter.visibility = View.VISIBLE
            if (!audioCutterViewModel.getData()) {
                ll_no_finish_task.visibility = View.VISIBLE
            } else {
                ll_no_finish_task.visibility = View.GONE
            }
            pb_audio_cutter.visibility = View.GONE

        }

        iv_check.setOnClickListener(View.OnClickListener {
            checkedAllStatus = !checkedAllStatus
            if (checkedAllStatus) {
                deleteItemStatus.postValue(DeleteState.CHECKED)
                listItemDelete.clear()
                var position: Int = 0
                while (position < sizeListDeleteAll) {
                    listItemDelete.add(position)
                    position++
                }
                // listItemDelete get all size --> delete all
            } else {
                deleteItemStatus.postValue(DeleteState.UNCHECK)
                listItemDelete.clear()
            }
        })
    }

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {

        when (fragmentMeta.action) {
            Constance.ACTION_DELETE -> {
                deleteItemStatus.postValue(DeleteState.UNCHECK)
            }
            Constance.ACTION_CANCEL_DELETE -> {
                deleteItemStatus.postValue(DeleteState.HIDE)
            }
            Constance.ACTION_DELETE_ALL -> {
                Log.d(TAG, "sssssssssss")
            }
        }
    }

    override fun play(audioFile: AudioFile) {
        runOnUI {
            ManagerFactory.getAudioPlayer().play(audioFile)
        }
    }

    override fun pause() {
        ManagerFactory.getAudioPlayer().pause()
    }

    override fun resume() {
        ManagerFactory.getAudioPlayer().resume()
    }

    override fun stop() {
        ManagerFactory.getAudioPlayer().stop()
    }

    override fun seekTo(position: Int) {
        ManagerFactory.getAudioPlayer().seek(position)
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

    //click chose button on adapter -> delete
    override fun isDelete(position: Int) {
        listItemDelete.add(position)
        for (item in listItemDelete)
            Log.d(TAG, "isCheckedDelete: possion: " + item)
    }

    //click chose button on adapter -> undelete
    override fun isUnDelete(position: Int) {
        listItemDelete.remove(position)
        for (item in listItemDelete)
            Log.d(TAG, "isCheckedDelete: possion: " + item)
    }

    // checked delete all
    override fun deleteAll(deleteStatus: Boolean) {
        if (deleteStatus) {
            iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
            checkedAllStatus = true

            listItemDelete.clear()
            var position: Int = 0
            while (position < sizeListDeleteAll) {
                listItemDelete.add(position)
                position++
            }

        } else {
            iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
            checkedAllStatus = false
        }
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
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
import com.example.audiocutter.functions.mystudio.AudioFileView
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

    val listAudioObserver = Observer<List<AudioFileView>> { listMusic ->
        if (listMusic.size == 0) {
            ll_no_finish_task.visibility = View.VISIBLE
        } else {
            ll_no_finish_task.visibility = View.GONE
        }
        audioCutterAdapter.submitList(ArrayList(listMusic))

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
            ViewModelProviders.of(this).get(AudioCutterViewModel::class.java)
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
            pb_audio_cutter.visibility = View.VISIBLE
            val listAudioViewLiveData = audioCutterViewModel.getData()
            listAudioViewLiveData.removeObserver(listAudioObserver)
            listAudioViewLiveData.observe(viewLifecycleOwner, listAudioObserver)
            pb_audio_cutter.visibility = View.GONE

        }

        iv_check.setOnClickListener(View.OnClickListener {
            audioCutterAdapter.submitList(audioCutterViewModel.clickSelectAllBtn())
            checkAllItemSelected()
        })
    }

    override fun onReceivedAction(fragmentMeta: FragmentMeta) {

        when (fragmentMeta.action) {
            Constance.ACTION_UNCHECK -> {
                audioCutterAdapter.submitList(audioCutterViewModel.changeAutoItemToDelete())
                cl_delete_all.visibility = View.VISIBLE
            }
            Constance.ACTION_HIDE -> {
                audioCutterAdapter.submitList(audioCutterViewModel.changeAutoItemToMore())
                cl_delete_all.visibility = View.GONE
                iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
            }
            Constance.ACTION_DELETE_ALL -> {
                audioCutterViewModel.deleteAllItemSelected()
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
    private fun checkAllItemSelected(){
        if(audioCutterViewModel.isAllChecked()){
            iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_checked)
        }else{
            iv_check.setImageResource(R.drawable.output_audio_manager_screen_icon_uncheck)
        }
    }
    override fun checkDeletePos(position: Int) {
        audioCutterAdapter.submitList(audioCutterViewModel.checkItemPosition(position))
        checkAllItemSelected()
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
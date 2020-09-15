package com.example.audiocutter.functions.mystudio.audiocutter

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.util.ContentLengthInputStream
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.DeleteState
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.fragment_audio_cutter.*
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.Toast
import com.example.audiocutter.functions.mystudio.dialog.*
import kotlinx.android.synthetic.main.output_audio_manager_screen.*
import kotlinx.coroutines.delay

class AudioCutterFragment() : BaseFragment(),
    AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener {

    val TAG = "giangtd"
    lateinit var audioCutterViewModel: AudioCutterViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    var visibilityDeleteStatus = false

    var deleteItemStatus: MutableLiveData<DeleteState> = MutableLiveData()

    private val deleteItemObserver = Observer<DeleteState> {
        audioCutterAdapter.updateDeleteStatus(it)
        when (it) {
            DeleteState.HIDE -> {
                cl_delete_all.visibility = View.GONE
                visibilityDeleteStatus = false
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
            // show loading
            pb_audio_cutter.visibility = View.VISIBLE
            audioCutterViewModel.getData()
            pb_audio_cutter.visibility = View.GONE
            // hide loadding
        }

        iv_check.setOnClickListener(View.OnClickListener {
            visibilityDeleteStatus = !visibilityDeleteStatus
            if (visibilityDeleteStatus) {
                deleteItemStatus.postValue(DeleteState.CHECKED)
            } else {
                deleteItemStatus.postValue(DeleteState.UNCHECK)
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
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.contacs -> {
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.open_with -> {
                    Log.d(TAG, "showMenu:cut ")
                }
                R.id.share -> {
                    Log.d(TAG, "showMenu:cut ")
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
                    Log.d(TAG, "showMenu:cut ")
                }
            }

            true
        })

        popup.show()
    }

    override fun onRenameClick() {
        Log.d(TAG, "onRenameClick: rename")
    }

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

}
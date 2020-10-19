package com.example.audiocutter.functions.mystudio.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.channel.FragmentMeta
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MyStudioFragmentBinding
import com.example.audiocutter.functions.mystudio.objects.AudioFileView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterAdapter
import com.example.audiocutter.functions.mystudio.adapters.AudioCutterScreenCallback
import com.example.audiocutter.functions.mystudio.adapters.ItemLoadingCallBack
import com.example.audiocutter.functions.mystudio.adapters.MyStudioAdapterItemLoading
import com.example.audiocutter.functions.mystudio.dialog.*
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import com.example.audiocutter.objects.AudioFile
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyStudioFragment() : BaseFragment(), AudioCutterScreenCallback, RenameDialogListener, SetAsDialogListener, DeleteDialogListener, ItemLoadingCallBack {

    private lateinit var binding: MyStudioFragmentBinding
    val TAG = "giangtd"
    lateinit var myStudioViewModel: MyStudioViewModel
    lateinit var audioCutterAdapter: AudioCutterAdapter
    lateinit var loadingItemAdapter: MyStudioAdapterItemLoading

    var typeAudio: Int = -1
    var isDoubleDeleteClicked = true
    var isLoading = false   // trang thai load cua progressbar

    var currentView: View? = null

    // observer data
    val listAudioObserver = Observer<List<AudioFileView>> { listMusic ->
        listMusic?.let {
            if (!listMusic.isEmpty()) {
                audioCutterAdapter.submitList(ArrayList(listMusic))
            }
        }
    }

    val listLoadingObserver = Observer<List<ConvertingItem>> { listLoading ->
        listLoading?.let {
            if (!listLoading.isEmpty()) {
//                listConverting.clear()
//                for (item in listLoading) {
//                    listConverting.add(item)
//                }
//                binding.rvLoadingItem.visibility = View.VISIBLE
                loadingItemAdapter.submitList(ArrayList(listLoading))
                Log.d(TAG, "submitList listLoadingObserver: ${listLoading.size} type ${typeAudio}")
            } else {
//                binding.rvLoadingItem.visibility = View.GONE
            }
        }
    }

    val listConverting: ArrayList<ConvertingItem> = ArrayList()
    /*
     val listLoadingObserver = Observer<List<ConvertingItem>> { listLoading ->

         listConverting.clear()
         for (item in listLoading) {
             listConverting.add(item)
         }
         listLoading?.let {
             if (!listLoading.isEmpty()) {
                 loadingItemAdapter.submitList(ArrayList(listLoading))
             } else {
             }
         }
         Log.d(TAG, "listLoading size : " + listLoading.size)
     }

    */

/*    private val progressObserver = Observer<ConvertingItem> {

//        CoroutineScope(Dispatchers.Main).launch {
            val newItem = ConvertingItem(it.id, it.state, it.percent, it.audioFile)

            var index = 0
            if (!listConverting.isEmpty()) {
                for (item in listConverting) {
                    if (item.id == newItem.id) {
                        listConverting[index] = newItem
                    }
                    index++
                }
            }
            loadingItemAdapter.submitList(listConverting)
//        }
        Log.d(TAG, "progressObserver percent: " + it.percent)
    }*/


    // observer playInfo mediaplayer
    private val playerInfoObserver = Observer<PlayerInfo> {
        if (myStudioViewModel.isPlayingStatus) {
            audioCutterAdapter.submitList(myStudioViewModel.updatePlayerInfo(it))
        }
    }

    private val progressObserver = Observer<ConvertingItem> {
        loadingItemAdapter.submitList(myStudioViewModel.updateLoadingEditor(it))
    }

    // observer loading sstatus
    private val loadingStatusObserver = Observer<Boolean> {
        if (it) {
            binding.pbAudioCutter.visibility = View.VISIBLE
        } else {
            binding.pbAudioCutter.visibility = View.GONE
        }
    }

    // observer is empty sstatus
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
            cl_delete_all.visibility = View.GONE
            binding.llNoFinishTask.visibility = View.VISIBLE
            Log.d(TAG, "VISIBLE: ")
        } else {
            binding.llNoFinishTask.visibility = View.GONE
            Log.d(TAG, "GONE: ")
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

        rv_loading_item.layoutManager = LinearLayoutManager(context)
        //rv_loading_item.setHasFixedSize(true)
        rv_loading_item.adapter = loadingItemAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myStudioViewModel = ViewModelProviders.of(this).get(MyStudioViewModel::class.java)
        audioCutterAdapter = AudioCutterAdapter(this)
        loadingItemAdapter = MyStudioAdapterItemLoading(this)

        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)

        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this as LifecycleOwner, progressObserver)

//        ManagerFactory.getAudioEditorManager().getListCuttingItems()
//            .observe(this as LifecycleOwner, listLoadingObserver)

//        myStudioViewModel.getListLoading(typeAudio)
//            .observe(this as LifecycleOwner, listLoadingObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_fragment, container, false)

        typeAudio = requireArguments().getInt(BUNDLE_NAME_KEY)  // lấy typeAudio của từng loại fragment
        runOnUI {
            val listAudioViewLiveData = myStudioViewModel.getData(typeAudio) // get data from funtion newIntance
            listAudioViewLiveData.observe(this as LifecycleOwner, listAudioObserver)

            myStudioViewModel.getListLoading(typeAudio)
                .observe(this as LifecycleOwner, listLoadingObserver)


//            ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
//                .observe(this as LifecycleOwner, progressObserver)

//            ManagerFactory.getAudioEditorManager().getListCuttingItems()
//                .observe(this as LifecycleOwner, listLoadingObserver)

            myStudioViewModel.getLoadingStatus().observe(viewLifecycleOwner, loadingStatusObserver)

            myStudioViewModel.getIsEmptyStatus().observe(viewLifecycleOwner, isEmptyStatusObserver)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
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

    // cancel loading item
    override fun cancel(id: Int) {
        Log.d(TAG, "cancel: canelllllllllll")
    }

}
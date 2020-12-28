package com.example.audiocutter.functions.audiochooser.screens

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.MergePreviewScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.MergePreviewAdapter
import com.example.audiocutter.functions.audiochooser.dialogs.MergeDialog
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterView
import com.example.audiocutter.ui.audiochooser.merge.MyItemTouchHelper
import com.example.audiocutter.ui.audiochooser.merge.WrapContentLinearLayoutManager
import com.example.audiocutter.util.Utils
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMergingConfig
import java.io.File


class MergePreviewScreen : BaseFragment(), MergePreviewAdapter.AudioMergeChooseListener,
    View.OnClickListener, MergeDialog.MergeDialogListener {

    private val safeArg: MergePreviewScreenArgs by navArgs()
    private lateinit var listAudioPath: Array<String>
    private lateinit var listPath: ArrayList<String>

    private val TAG = "manhqn"

    private lateinit var binding: MergePreviewScreenBinding
    private lateinit var audioMerAdapter: MergePreviewAdapter
    private lateinit var audioMerModel: MergePreviewModel
    var currentPos = -1

    private lateinit var mergeDialog: MergeDialog

//    private val playerInfoObserver = Observer<PlayerInfo> {
//        audioMerAdapter.submitList(audioMerModel.updateMediaInfo(it))
//    }

    override fun onPause() {
        super.onPause()
        audioMerModel.pause()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerModel = ViewModelProvider(this).get(MergePreviewModel::class.java)
        audioMerAdapter = MergePreviewAdapter(requireContext(), audioMerModel.getAudioPlayer(), lifecycleScope,requireActivity())
//        ManagerFactory.getDefaultAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)

        listPath = ArrayList()
        listAudioPath = safeArg.listaudio
        val newListAudio = ArrayList<AudioCutterView>()


        for (item in listAudioPath) {
            listPath.add(item)
            val audioFile = ManagerFactory.getAudioFileManager().findAudioFile(item)
            audioFile?.let {
                listPath.add(item)
                newListAudio.add(AudioCutterView(audioFile))
            }
        }

        val listData = audioMerModel.getListAudioTmp()
        listData.forEach {

            Log.d(TAG, "onPostCreate: ${it.audioFile.fileName}")
        }
        receiveData(newListAudio)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.merge_preview_screen, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLists()
    }


    private fun initViews() {
//        mergeDialog = MergeDialog(requireContext())
//        mergeDialog.setOnCallBack(this)

        binding.btMergeAudio.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        audioMerAdapter.setOnCallBack(this)
        binding.ivAddfileMerge.setOnClickListener(this)
    }

    private fun initLists() {

        val callBack = MyItemTouchHelper(audioMerAdapter,requireContext())
        val itemTouchHelper = ItemTouchHelper(callBack)
        audioMerAdapter.setTouchHelper(itemTouchHelper)
        binding.rvMergeChoose.adapter = audioMerAdapter
        itemTouchHelper.attachToRecyclerView(binding.rvMergeChoose)
        binding.rvMergeChoose.setHasFixedSize(true)
        binding.rvMergeChoose.layoutManager =
            WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    override fun play(pos: Int) {
        runOnUI {
            currentPos = pos
            audioMerModel.play(pos)
        }
    }

    override fun pause(pos: Int) {
        currentPos = pos
        audioMerModel.pause()

    }

    override fun resume(pos: Int) {
        currentPos = pos
        audioMerModel.resume()
    }

    override fun deleteAudio(audioFile: AudioCutterView) {
        try {
            sendFragmentAction(MergeChooserScreen::class.java.name, "ACTION_DELETE", audioFile)
            ManagerFactory.getDefaultAudioPlayer().stop()
            audioMerAdapter.submitList(audioMerModel.removeItemAudio(audioFile))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun moveItemAudio(prePos: Int, nextPos: Int) {
        audioMerAdapter.submitList(audioMerModel.moveItemAudio(prePos, nextPos))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_mer_screen_back -> {
                activity?.onBackPressed()
            }
            R.id.iv_addfile_merge -> {
                activity?.onBackPressed()
            }
            R.id.bt_merge_audio -> mergeAudioFile()
        }
    }

    private fun mergeAudioFile() {
        if (audioMerModel.getListAudio().size >= 2) {

//            mergeDialog.sendData(audioMerModel.getListAudio().size)
//            mergeDialog.show()

            val dialog = MergeDialog.newInstance(
                this, audioMerModel.getListAudio().size, Utils.getBaseName(
                    File(listPath[0])
                )
            )
            dialog.show(childFragmentManager, MergeDialog.TAG)

            showKeybroad()

        } else {
            showToast("You need to select 2 or more files")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        audioMerModel.stop()
    }

    fun receiveData(listData: List<AudioCutterView>) {
        audioMerModel.initListFileAudio(listData)
        audioMerAdapter.submitList(audioMerModel.getListAudio())
    }

    override fun mergeAudioFile(filename: String) {
        val mergingConfig = AudioMergingConfig(
            AudioFormat.MP3,
            filename,
            ManagerFactory.getAudioFileManager().getFolderPath(Folder.TYPE_MERGER)
        )
        viewStateManager.editorSaveMergingAudio(this, audioMerModel.getListAudio(), mergingConfig)
    }

    override fun cancalKeybroad() {
        hideKeyBroad()
    }

    private fun hideKeyBroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeybroad() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


}

package com.example.audiocutter.functions.audiochooser.merge.screen

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.MergePreviewScreenBinding
import com.example.audiocutter.functions.audiochooser.cut.objs.AudioCutterView
import com.example.audiocutter.functions.audiochooser.merge.adapters.MergeChooseAdapter
import com.example.audiocutter.functions.audiochooser.merge.dialogs.MergeDialog
import com.example.audiocutter.functions.audiochooser.merge.event.OnActionCallback
import com.example.audiocutter.functions.audiochooser.merge.widget.MyItemTouchHelper
import com.example.audiocutter.functions.audiochooser.merge.widget.WrapContentLinearLayoutManager

class MergePreviewScreen : BaseFragment(), MergeChooseAdapter.AudioMergeChooseListener,
    View.OnClickListener, MergeDialog.MergeDialogListener {
    private val TAG = "manhqn"
    private lateinit var binding: MergePreviewScreenBinding
    private lateinit var audioMerAdapter: MergeChooseAdapter
    private lateinit var audioMerModel: MergePreviewModel
    var currentPos = -1
    private lateinit var mCallback: OnActionCallback

    private lateinit var mergeDialog: MergeDialog


    fun setOnCalBack(event: OnActionCallback) {
        mCallback = event
    }

    private val playerInfoObserver = Observer<PlayerInfo> {
        audioMerAdapter.submitList(audioMerModel.updateMediaInfo(it))
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerAdapter =
            MergeChooseAdapter(
                requireContext()
            )
        audioMerModel = ViewModelProvider(this).get(MergePreviewModel::class.java)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(this, playerInfoObserver)
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
        mergeDialog =
            MergeDialog(
                requireContext()
            )
        mergeDialog.setOnCallBack(this)

        binding.btMergeAudio.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        audioMerAdapter.setOnCallBack(this)
        binding.ivAddfileMerge.setOnClickListener(this)



    }


    private fun initLists() {

        val callBack = MyItemTouchHelper(audioMerAdapter)
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

    override fun deleteAudio(pos: Int) {
        ManagerFactory.getAudioPlayer().stop()
        audioMerAdapter.submitList(audioMerModel.removeItemAudio(pos))
    }

    override fun moveItemAudio(prePos: Int, nextPos: Int) {
        audioMerAdapter.submitList(audioMerModel.moveItemAudio(prePos, nextPos))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_mer_screen_back -> mCallback.backFrg()
            R.id.iv_addfile_merge -> mCallback.backFrg()
            R.id.bt_merge_audio -> mergeAudioFile()
        }
    }

    private fun mergeAudioFile() {
        if (audioMerModel.getListAudio().size >= 2) {
            mergeDialog.sendData(audioMerModel.getListAudio().size)
            mergeDialog.show()
            showKeybroad()
        } else {
            showToast("You need to select 2 or more files")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ManagerFactory.getAudioPlayer().stop()
    }

    fun receiveData(listData: List<AudioCutterView>) {
        audioMerModel.initListFileAudio(listData)
        audioMerAdapter.submitList(audioMerModel.getListAudio())
    }

    override fun mergeAudioFile(filename: String) {
        Log.d(TAG, "mergeAudioFile: $filename size list ${audioMerModel.getListAudio().size}")
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

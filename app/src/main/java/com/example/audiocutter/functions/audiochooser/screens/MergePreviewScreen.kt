package com.example.audiocutter.functions.audiochooser.screens

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.databinding.MergePreviewScreenBinding
import com.example.audiocutter.functions.audiochooser.adapters.MergePreviewAdapter
import com.example.audiocutter.functions.audiochooser.dialogs.MergeDialog
import com.example.audiocutter.functions.audiochooser.objects.AudioCutterViewItem
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.ui.audiochooser.merge.WrapContentLinearLayoutManager
import com.example.core.core.AudioFormat
import com.example.core.core.AudioMergingConfig
import com.google.android.material.snackbar.Snackbar

class MergePreviewScreen : BaseFragment(), MergePreviewAdapter.AudioMergeChooseListener,
    View.OnClickListener, MergeDialog.MergeDialogListener {

    private lateinit var binding: MergePreviewScreenBinding
    private lateinit var audioMerAdapter: MergePreviewAdapter

    //        private lateinit var audioMerModel: MergePreviewModel
    private val audioMerModel: MergeChooserModel by navGraphViewModels(R.id.mer_navigation)

    //    private var toast: Toast? = null
    private lateinit var mergeDialog: MergeDialog

    override fun onPause() {
        super.onPause()
        audioMerModel.pause()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        audioMerAdapter = MergePreviewAdapter(
            requireContext(),
            audioMerModel.getAudioPlayer(),
            lifecycleScope,
            requireActivity()
        )
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
        observerData()

    }
    private fun observerData() {
        audioMerModel.listAudioCutterViewItemsSelected.observe(viewLifecycleOwner){
            if(it.isEmpty()){
                binding.rvMergeChoose.visibility = View.INVISIBLE
                binding.rltEmpty.visibility = View.VISIBLE
            }else{
                binding.rvMergeChoose.visibility = View.VISIBLE
                binding.rltEmpty.visibility = View.INVISIBLE
            }
            audioMerAdapter.submitList(it)
        }
        audioMerModel.checkLessThanTwoItemsIsSelected.observe(viewLifecycleOwner){
            if(it){
                showNotification(getString(R.string.rule_amout_item_mer))
            }
        }

        audioMerModel.showMergingDialog.observe(viewLifecycleOwner){
            val dialog = MergeDialog.newInstance(
                this,
                it.totalItemSelected,
                it.suggestionName
            )
            dialog.show(childFragmentManager, MergeDialog.TAG)
            showKeybroad()
        }

        audioMerModel.onMergingButtonClicked.observe(viewLifecycleOwner){
            viewStateManager.editorSaveMergingAudio(
                this,
                it.listItemsSlected,
                it.mergingConfig
            )
        }
    }

    private fun initViews() {
        binding.btMergeAudio.setOnClickListener(this)
        binding.ivMerScreenBack.setOnClickListener(this)
        binding.addFileTv.setOnClickListener(this)
        audioMerAdapter.setOnCallBack(this)
        binding.ivAddfileMerge.setOnClickListener(this)
        audioMerModel.checkNextButtonEnable.observe(viewLifecycleOwner) {
            binding.btMergeAudio.isEnabled = it
            if(it){
                binding.btMergeAudio.setTextColor(Color.parseColor("#ffffff"))
            }else{
                binding.btMergeAudio.setTextColor(Color.parseColor("#8C8C8C"))
            }
          /*  if (it) {
                setColorButtonNext(R.color.colorWhite, R.drawable.bg_next_audio_enabled, true)
            } else {
                setColorButtonNext(R.color.colorgray, R.drawable.bg_next_audio_disabled, false)
            }*/
        }
    }

    private fun initLists() {
        binding.rvMergeChoose.adapter = audioMerAdapter
        audioMerAdapter.itemTouchHelper.attachToRecyclerView(binding.rvMergeChoose)
        binding.rvMergeChoose.setHasFixedSize(true)
        binding.rvMergeChoose.layoutManager =
            WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    override fun deleteAudio(audioFile: AudioCutterViewItem) {
        audioMerModel.removeItemAudio(audioFile)

    }

    override fun moveItemAudio(prePos: Int, nextPos: Int) {
        audioMerModel.swapItemAudio(prePos, nextPos)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_mer_screen_back -> {
                activity?.onBackPressed()
            }
            R.id.iv_addfile_merge -> {
                ManagerFactory.getDefaultAudioPlayer().stop()
                activity?.onBackPressed()
            }
            R.id.bt_merge_audio -> {
                audioMerModel.clickedOnMergingButton()
            }
            R.id.add_file_tv ->{
                ManagerFactory.getDefaultAudioPlayer().stop()
                activity?.onBackPressed()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        audioMerModel.stop()
    }

    override fun mergeAudioFile(filename: String) {
        audioMerModel.onMergingDialogResult(filename)
    }

    override fun cancelKeybroad() {
        hideKeyBroad()
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
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

    private fun setColorButtonNext(color: Int, bg: Int, rs: Boolean) {
        binding.btMergeAudio.isEnabled = rs
        binding.btMergeAudio.background = (ContextCompat.getDrawable(requireContext(), bg))
//        binding.bt_merge_audio.setTextColor(ContextCompat.getColor(requireContext(), color))
    }
}

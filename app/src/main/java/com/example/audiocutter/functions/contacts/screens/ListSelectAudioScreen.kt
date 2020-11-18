package com.example.audiocutter.functions.contacts.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.databinding.ListContactSelectScreenBinding
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.contacts.adapters.SelectAudioScreenCallback
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.util.FileUtils
import kotlinx.android.synthetic.main.list_contact_select_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ListSelectAudioScreen() : BaseFragment(), SelectAudioScreenCallback, View.OnClickListener {
    val TAG = "giangtd"
    val safeArg: ListSelectAudioScreenArgs by navArgs()
    lateinit var mListSelectAudioViewModel: ListSelectAudioViewModel
    lateinit var mListSelectAdapter: ListSelectAdapter
    val REQ_CODE_PICK_SOUNDFILE = 1990
    var positionSelect = -1
    lateinit var binding: ListContactSelectScreenBinding

    // observer data
    val listAudioObserver = Observer<List<SelectItemView>> { listAudio ->

        if (listAudio != null) {
            mListSelectAdapter.submitList(ArrayList(listAudio))
        }
    }

    // observer loading status
    private val loadingStatusObserver = Observer<Boolean> {
        if (it) {
            binding.pbSelect.visibility = View.VISIBLE
        } else {
            binding.pbSelect.visibility = View.GONE
            Log.d(TAG, "5555555555555555555555: ")
            binding.rvListSelectAudio.scrollToPosition(mListSelectAudioViewModel.getIndexSelectRingtone(safeArg.uri))       // set vi tri khi chuyen sang man hinh den bai nhac da duoc chon
        }
    }

    // observer is empty status
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
            binding.clSelect.visibility = View.GONE
            binding.clBottom.visibility = View.GONE
            binding.clNoAudio.visibility = View.VISIBLE
        } else {
            binding.clSelect.visibility = View.VISIBLE
            binding.clBottom.visibility = View.VISIBLE
            binding.clNoAudio.visibility = View.GONE
        }
    }

    fun init() {
        binding.rvListSelectAudio.layoutManager = LinearLayoutManager(context)
        binding.rvListSelectAudio.setHasFixedSize(true)
        binding.rvListSelectAudio.adapter = mListSelectAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_contact_select_screen, container, false)

        mListSelectAudioViewModel.getListAudioFile().observe(viewLifecycleOwner, listAudioObserver)

        mListSelectAudioViewModel.getIsEmptyStatus()
            .observe(viewLifecycleOwner, isEmptyStatusObserver)
        mListSelectAudioViewModel.getLoadingStatus()
            .observe(viewLifecycleOwner, loadingStatusObserver)

        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mListSelectAudioViewModel = ViewModelProviders.of(this)
            .get(ListSelectAudioViewModel::class.java)
        mListSelectAdapter = ListSelectAdapter(this)

        val fileUri = safeArg.uri
        mListSelectAudioViewModel.init(fileUri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        // sai do chua load duoc het item
//        binding.rvListSelectAudio.scrollToPosition(mListSelectAudioViewModel.getIndexSelectRingtone(safeArg.uri))       // set vi tri khi chuyen sang man hinh den bai nhac da duoc chon

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.ivFile.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(textChange: CharSequence, start: Int, before: Int, count: Int) {
                if (mListSelectAudioViewModel.searchAudioFile(textChange.toString()).size <= 0) {
                    binding.clSelect.visibility = View.GONE
                    binding.clBottom.visibility = View.GONE
                    binding.clNoAudio.visibility = View.VISIBLE
                } else {
                    binding.clSelect.visibility = View.VISIBLE
                    binding.clBottom.visibility = View.VISIBLE
                    binding.clNoAudio.visibility = View.GONE
                    mListSelectAdapter.submitList(mListSelectAudioViewModel.searchAudioFile(textChange.toString()))
                }
            }
        })
    }

    override fun play(position: Int) {
        mListSelectAudioViewModel.playAudio(position)
    }

    override fun pause() {
        mListSelectAudioViewModel.pauseAudio()
    }

    override fun resume() {
        mListSelectAudioViewModel.resumeAudio()
    }

    override fun stop(position: Int) {
        mListSelectAudioViewModel.stopAudio(position)

    }

    override fun seekTo(cusorPos: Int) {
        mListSelectAudioViewModel.seekToAudio(cusorPos)
    }

    override fun isShowPlayingAudio(positition: Int) {
        positionSelect = positition
        mListSelectAdapter.submitList(mListSelectAudioViewModel.showPlayingAudio(positition))
    }

    override fun isSelect(position: Int) {

        mListSelectAdapter.submitList(mListSelectAudioViewModel.selectAudio(position))
    }


    override fun onClick(view: View?) {
        when (view) {
            binding.ivSearch -> {
                showKeyboard()
                binding.clDefault.visibility = View.INVISIBLE
                binding.clSearch.visibility = View.VISIBLE
            }
            binding.ivSearchClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clSearch.visibility = View.INVISIBLE
                binding.edtSearch.text.clear()
                hideKeyboard()
            }
            binding.ivClear -> {
                binding.edtSearch.text.clear()
            }
            binding.btnSave -> {
                if (mListSelectAudioViewModel.setRingtone(safeArg.phoneNumber)) {
                    Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
                }
                requireActivity().onBackPressed()
            }
            binding.ivFile -> {
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                }/* else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                } */
                else {
                    intent = Intent(Intent.ACTION_GET_CONTENT)
                }
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.type = "audio/*"
//                intent.type = "audio/mp3"
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select a File "), REQ_CODE_PICK_SOUNDFILE)

            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK && intent != null) {

            val path = FileUtils.getPath(requireContext(), intent.data!!)

            path?.let {
                if (mListSelectAudioViewModel.setRingtoneWithUri(safeArg.phoneNumber, path)) {
                    Toast.makeText(context, "Set Ringtone Success !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Set Ringtone Failure !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.edtSearch.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onPause() {
        super.onPause()
        if (positionSelect > -1) {
            mListSelectAudioViewModel.stopAudio(positionSelect)
        }
    }
}
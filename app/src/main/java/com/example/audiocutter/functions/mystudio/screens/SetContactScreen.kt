package com.example.audiocutter.functions.mystudio.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.MyStudioContactScreenBinding
import com.example.audiocutter.functions.mystudio.adapters.SetContactAdapter
import com.example.audiocutter.functions.mystudio.adapters.SetContactCallback
import com.example.audiocutter.functions.mystudio.objects.SetContactItemView
import com.example.audiocutter.functions.resultscreen.screens.ResultScreenArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SetContactScreen : BaseFragment(), SetContactCallback, View.OnClickListener {

    private lateinit var binding: MyStudioContactScreenBinding

    private val TAG = "giangtd4"
    private lateinit var listContactAdapter: SetContactAdapter
    private lateinit var mListContactViewModel: SetContactViewModel
    private val safeArg: SetContactScreenArgs by navArgs()      // truyen du lieu qua navigation
    private var isSearchStatus = false

    companion object {
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"

        @JvmStatic
        fun newInstance(pathUri: String): SetContactScreen {
            val contact = SetContactScreen()
            val bundle = Bundle()
            bundle.putString(BUNDLE_NAME_KEY, pathUri)
            contact.arguments = bundle
            return contact
        }
    }

    // observer data
    private val listContactObserver = Observer<List<SetContactItemView>> { listContact ->
        if (listContact != null) {
            listContactAdapter.submitList(ArrayList(listContact))
//            if (isSearchStatus) {
//                binding.rvListContact.post {
//                    binding.rvListContact.smoothScrollToPosition(0)
//                }
//            }

        }
    }

    // observer loading status
    private val loadingStatusObserver = Observer<Boolean> {
        if (it) {
            binding.pbAudioCutter.visibility = View.VISIBLE
        } else {
            binding.pbAudioCutter.visibility = View.GONE
        }
    }

    // observer is empty status
    private val isEmptyStatusObserver = Observer<Boolean> {
        if (it) {
            binding.clContact.visibility = View.INVISIBLE
            binding.clNoContact.visibility = View.VISIBLE
        } else {
            binding.clContact.visibility = View.VISIBLE
            binding.clNoContact.visibility = View.INVISIBLE
        }
    }


    private val isSelectObserver = Observer<Boolean> { status ->
        if (status) {
            binding.ivOk.visibility = View.VISIBLE
            binding.tvOk.visibility = View.VISIBLE

            binding.ivNotOk.visibility = View.GONE
            binding.tvNotOk.visibility = View.GONE
        }
    }

    private val adapterObserver = object: RecyclerView.AdapterDataObserver(){
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            binding.rvListContact.scrollToPosition(0)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            binding.rvListContact.scrollToPosition(0)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            binding.rvListContact.scrollToPosition(0)
        }
    }

    private fun init() {
        binding.rvListContact.layoutManager = LinearLayoutManager(context)
        binding.rvListContact.setHasFixedSize(true)
        binding.rvListContact.adapter = listContactAdapter

        listContactAdapter.registerAdapterDataObserver(adapterObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.my_studio_contact_screen, container, false)

        runOnUI {
            // loi observe hoi lai tai
            mListContactViewModel.getIsEmptyStatus()
                .observe(viewLifecycleOwner, isEmptyStatusObserver)

            mListContactViewModel.getLoadingStatus()
                .observe(viewLifecycleOwner, loadingStatusObserver)

            mListContactViewModel.getIsSelectItem().observe(viewLifecycleOwner, isSelectObserver)
        }

        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mListContactViewModel = ViewModelProviders.of(this).get(SetContactViewModel::class.java)

        listContactAdapter = SetContactAdapter(this)


//        lifecycleScope.launch {
        /*delay(250)*/
        mListContactViewModel.scan()
//        }

        mListContactViewModel.getData()
            .observe(
                this as LifecycleOwner,
                listContactObserver
            )          // loi observe hoi lai tai

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        binding.ivOk.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(
                textChange: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
//                if (isSearchStatus) {
//                    binding.rvListContact.post {
//                        binding.rvListContact.smoothScrollToPosition(0)
//                    }
//                }
                mListContactViewModel.searchContact(textChange.toString())
                if (textChange.toString() != "") {
                    binding.ivClear.visibility = View.VISIBLE
                } else {
                    binding.ivClear.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun itemOnClick(phoneNumber: String) {
        mListContactViewModel.updateIsSelectItem(phoneNumber)
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        hideKeyboard()
    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivSearch -> {
                binding.clDefault.visibility = View.GONE
                binding.clSearch.visibility = View.VISIBLE
                showKeyboard()
                isSearchStatus = true
            }
            binding.ivSearchClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clSearch.visibility = View.GONE
                binding.edtSearch.text.clear()
                hideKeyboard()
                isSearchStatus = false
//                binding.rvListContact.post {
//                    binding.rvListContact.smoothScrollToPosition(0)
//                }

            }
            binding.ivClear -> {
                binding.edtSearch.text.clear()
            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
            binding.ivOk -> {
                if (mListContactViewModel.setRingtoneForContact(safeArg.pathUri)) {
//                    Toast.makeText(context, getString(R.string.set_contact_ringtone_screen_set_ringtone_successfull), Toast.LENGTH_SHORT)
//                        .show()
                    context?.let {
                        showNotification(getString(R.string.set_contact_ringtone_screen_set_ringtone_successfull))
                    }
                    requireActivity().onBackPressed()
                } else {
                    context?.let {
                        showNotification(getString(R.string.set_contact_ringtone_screen_set_ringtone_fail))
                    }
//                    Toast.makeText(context, getString(R.string.set_contact_ringtone_screen_set_ringtone_fail), Toast.LENGTH_SHORT)
//                        .show()
                }
            }
        }
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        listContactAdapter.unregisterAdapterDataObserver(adapterObserver)
    }
}
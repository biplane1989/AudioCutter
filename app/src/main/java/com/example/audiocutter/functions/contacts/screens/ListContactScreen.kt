package com.example.audiocutter.functions.contacts.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.ListContactScreenBinding
import com.example.audiocutter.functions.contacts.adapters.ContactCallback
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.functions.contacts.adapters.ListContactAdapter
import kotlinx.coroutines.launch


class ListContactScreen() : BaseFragment(), ContactCallback, View.OnClickListener {

    private lateinit var binding: ListContactScreenBinding
    private val TAG = "giangtd4"
    private lateinit var listContactAdapter: ListContactAdapter
    private lateinit var mListContactViewModel: ListContactViewModel

    // observer data
    private val listContactObserver = Observer<List<ContactItemView>> { listContact ->
        if (listContact != null) {
            listContactAdapter.submitList(ArrayList(listContact))

            if (mListContactViewModel.getPhoneSelect() != "") {
                val index = getPositionSelect(listContact, mListContactViewModel.getPhoneSelect())
                if (index != -1) {
                    binding.rvListContact.post {
                        binding.rvListContact.smoothScrollToPosition(index)
                    }
                }
            }
        }
    }

    private fun getPositionSelect(listContact: List<ContactItemView>, phoneNumber: String): Int {
        var index = 0
        for (item in listContact) {
            if (item.contactItem.phoneNumber.equals(phoneNumber)) return index
            index++
        }
        return -1
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
            binding.clContact.visibility = View.GONE
            binding.clNoContact.visibility = View.VISIBLE
        } else {
            binding.clContact.visibility = View.VISIBLE
            binding.clNoContact.visibility = View.GONE
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_contact_screen, container, false)

        runOnUI {
            mListContactViewModel.refesherData()
            mListContactViewModel.getIsEmptyStatus()
                .observe(viewLifecycleOwner, isEmptyStatusObserver)

            mListContactViewModel.getLoadingStatus()
                .observe(viewLifecycleOwner, loadingStatusObserver)
        }
        return binding.root
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mListContactViewModel = ViewModelProviders.of(this).get(ListContactViewModel::class.java)
        listContactAdapter = ListContactAdapter(context, this)

        lifecycleScope.launch {
//            delay(1250)
            mListContactViewModel.scan()
        }

        mListContactViewModel.getData()
            .observe(this, listContactObserver)          // loi observe hoi lai tai
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(textChange: CharSequence, start: Int, before: Int, count: Int) {
                mListContactViewModel.searchContact(textChange.toString())
//                binding.rvListContact.post {
//                    binding.rvListContact.smoothScrollToPosition(0)
//                }
                if (textChange.toString() != "") {
                    binding.ivClear.visibility = View.VISIBLE
                } else {
                    binding.ivClear.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun itemOnClick(phoneNumber: String, ringtonePath: String) {
        mListContactViewModel.setPhoneSelect(phoneNumber)
        hideKeyboard()
        viewStateManager.contactScreenOnItemClicked(this, phoneNumber, ringtonePath)
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
            }
            binding.ivSearchClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clSearch.visibility = View.GONE
                binding.edtSearch.text.clear()
                hideKeyboard()
            }
            binding.ivClear -> {
                binding.edtSearch.text.clear()
            }
            binding.backButton -> {
                requireActivity().onBackPressed()
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

    override fun onDestroyView() {
        super.onDestroyView()
        listContactAdapter.unregisterAdapterDataObserver(adapterObserver)
    }
}
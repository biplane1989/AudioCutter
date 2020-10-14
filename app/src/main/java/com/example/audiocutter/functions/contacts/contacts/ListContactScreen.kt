package com.example.audiocutter.functions.contacts.contacts

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ContactManagerImpl
import com.example.audiocutter.databinding.ListContactScreenBinding
import kotlinx.android.synthetic.main.list_contact_screen.*


class ListContactScreen() : BaseFragment(), ContactCallback, View.OnClickListener {
    private lateinit var binding:ListContactScreenBinding
//    val callBack = mainCallBack

    // xin quyen
    val KEY = 1
    val CODE_WRITE_SETTINGS_PERMISSION = 2
    var isLoading = false   // trang thai load cua progressbar
    val TAG = "giangtd4"
    lateinit var listContactAdapter: ListContactAdapter
    lateinit var mlistContactViewModel: ListContactViewModel
    private var listContact: LiveData<List<ContactItemView>>? = null

    // observer data
    val listContactObserver = Observer<List<ContactItemView>> { listContact ->
//        Log.d(TAG, "listContactObserver: @@@@@@@@@@@@@@@@")
//        Log.d(TAG, "ringtone : "+ listContact.get(0).contactItem.ringtone)
        if (listContact != null) {
            runOnUI {
                if (listContact.isEmpty()) {
                    cl_no_contact.visibility = View.VISIBLE
                    cl_contact.visibility = View.GONE
                } else {
                    cl_contact.visibility = View.VISIBLE
                    cl_no_contact.visibility = View.GONE
                    listContactAdapter.submitList(ArrayList(listContact))
                }
            }
        }

    }

    fun init() {
        rv_list_contact.layoutManager = LinearLayoutManager(context)
        rv_list_contact.setHasFixedSize(true)
        rv_list_contact.adapter = listContactAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.list_contact_screen, container, false)
        ContactManagerImpl.registerContentObserVerDeleted()
        listContact?.observe(this.viewLifecycleOwner, listContactObserver)
        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mlistContactViewModel = ViewModelProviders.of(this)
            .get(ListContactViewModel::class.java)

        listContactAdapter = ListContactAdapter(context, this)
        isLoading = true
        runOnUI {
            listContact = mlistContactViewModel.getData() // get data from funtion newIntance
            listContact?.observe(this.viewLifecycleOwner, listContactObserver)

            isLoading = false
            binding.pbAudioCutter.visibility = View.GONE
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.ivSearch.setOnClickListener(this)
        binding.ivSearchClose.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        if (isLoading) {
            pb_audio_cutter.visibility = View.VISIBLE
        } else {
            pb_audio_cutter.visibility = View.GONE
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(textChange: CharSequence, start: Int, before: Int, count: Int) {
                if (mlistContactViewModel.searchContact(textChange.toString()).size <= 0) {
                    cl_contact.visibility = View.GONE
                    cl_no_contact.visibility = View.VISIBLE
                } else {
                    cl_contact.visibility = View.VISIBLE
                    cl_no_contact.visibility = View.GONE
                    listContactAdapter.submitList(mlistContactViewModel.searchContact(textChange.toString()))
                }

            }
        })
    }

    override fun itemOnClick(phoneNumber: String, uri: String) {
        hideKeyboard()
        viewStateManager.contactScreenOnItemClicked(this, phoneNumber, uri)
    }

    override fun onPostDestroy() {
        super.onPostDestroy()
        hideKeyboard()
    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivSearch -> {
                cl_default.visibility = View.GONE
                cl_search.visibility = View.VISIBLE
                showKeyboard()
            }
            binding.ivSearchClose -> {
                cl_default.visibility = View.VISIBLE
                cl_search.visibility = View.GONE
                edt_search.text.clear()
                hideKeyboard()
            }
            binding.ivClear -> {
                edt_search.text.clear()
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
        edt_search.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}
package com.example.audiocutter.functions.contactscreen.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.audioManager.AudioFileManagerImpl
import com.example.audiocutter.core.manager.ContactManagerImpl
import kotlinx.android.synthetic.main.list_contact_screen.*


class ListContactScreen(mainCallBack: mainCallBack) : BaseFragment(), ContactCallback {

    val callBack = mainCallBack

    val TAG = "giangtd"
    lateinit var listContactAdapter: ListContactAdapter
    lateinit var mlistContactViewModel: ListContactViewModel

    // observer data
    val listContactObserver = Observer<List<ContactItemView>> { listContact ->
        if (listContact.size <= 0) {
            rv_list_contact.visibility = View.GONE
            cl_no_contact.visibility = View.VISIBLE
        } else {
            rv_list_contact.visibility = View.VISIBLE
            cl_no_contact.visibility = View.GONE
            listContactAdapter.submitList(ArrayList(listContact))
        }
    }

    fun init() {
        rv_list_contact.layoutManager = LinearLayoutManager(context)
        rv_list_contact.setHasFixedSize(true)
        rv_list_contact.adapter = listContactAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mView = inflater.inflate(R.layout.list_contact_screen, container, false)
        ContactManagerImpl.registerContentObserVerDeleted()
        return mView
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mlistContactViewModel = ViewModelProviders.of(this)
            .get(ListContactViewModel(activity!!.application)::class.java)
        listContactAdapter = ListContactAdapter(context, this)
        runOnUI {
            val listContact = mlistContactViewModel.getData() // get data from funtion newIntance
            listContact.observe(this as LifecycleOwner, listContactObserver)
//            ContactManagerImpl.getListContact(requireContext())
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        iv_search.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.GONE
            cl_search.visibility = View.VISIBLE
        })

        iv_search_close.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.VISIBLE
            cl_search.visibility = View.GONE
        })

        iv_clear.setOnClickListener(View.OnClickListener {
            edt_search.text.clear()
        })

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mlistContactViewModel.searchContact(edt_search.text.toString()).size <= 0) {
                    rv_list_contact.visibility = View.GONE
                    cl_no_contact.visibility = View.VISIBLE
                } else {
                    rv_list_contact.visibility = View.VISIBLE
                    cl_no_contact.visibility = View.GONE
                    listContactAdapter.submitList(mlistContactViewModel.searchContact(edt_search.text.toString()))
                }
            }
        })

    }

    override fun itemOnClick(phoneNumber: String, uri: String) {
        Log.d(TAG, "itemOnClick: $$$$$$$$$$$$")
        callBack.item(phoneNumber, uri)

    }

    interface mainCallBack {
        fun item(phone: String, uri: String)
    }
}
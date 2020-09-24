package com.example.audiocutter.functions.contactscreen.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ContactManagerImpl
import com.example.audiocutter.functions.contactscreen.ContactItemView
import com.example.audiocutter.functions.mystudioscreen.AudioFileView
import com.example.audiocutter.functions.mystudioscreen.fragment.MyStudioViewModel
import kotlinx.android.synthetic.main.list_contact_screen.*
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.delay
import java.util.function.Consumer

class ListContactScreen : BaseFragment() {

    val TAG = "giangtd"
    lateinit var listContactAdapter: ListContactAdapter
    lateinit var mlistContactViewModel: ListContactViewModel

    // observer data
    val listContactObserver = Observer<List<ContactItemView>> { listContact ->
        listContactAdapter.submitList(ArrayList(listContact))
    }

    fun init() {
        rv_list_contact.layoutManager = LinearLayoutManager(context)
        rv_list_contact.setHasFixedSize(true)
        rv_list_contact.adapter = listContactAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_contact_screen, container, false)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mlistContactViewModel = ViewModelProviders.of(this)
            .get(ListContactViewModel(activity!!.application)::class.java)
        listContactAdapter = ListContactAdapter(context)
        runOnUI {
            val listContact = mlistContactViewModel.getData() // get data from funtion newIntance
            listContact.observe(this as LifecycleOwner, listContactObserver)
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


    }
}
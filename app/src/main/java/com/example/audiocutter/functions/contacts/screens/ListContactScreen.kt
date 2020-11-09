package com.example.audiocutter.functions.contacts.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.contact.ContactManagerImpl
import com.example.audiocutter.databinding.ListContactScreenBinding
import com.example.audiocutter.functions.contacts.adapters.ContactCallback
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.functions.contacts.adapters.ListContactAdapter
import kotlinx.android.synthetic.main.list_contact_screen.*
import kotlinx.android.synthetic.main.list_contact_screen.pb_audio_cutter
import kotlinx.android.synthetic.main.my_studio_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ListContactScreen() : BaseFragment(), ContactCallback, View.OnClickListener {

    private lateinit var binding: ListContactScreenBinding
    val TAG = "giangtd4"
    lateinit var listContactAdapter: ListContactAdapter
    lateinit var mListContactViewModel: ListContactViewModel

    // observer data
    val listContactObserver = Observer<List<ContactItemView>> { listContact ->
        if (listContact != null) {
            listContactAdapter.submitList(ArrayList(listContact))
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
            binding.clContact.visibility = View.GONE
            binding.clNoContact.visibility = View.VISIBLE
        } else {
            binding.clContact.visibility = View.VISIBLE
            binding.clNoContact.visibility = View.GONE
        }
    }


    fun init() {
        binding.rvListContact.layoutManager = LinearLayoutManager(context)
        binding.rvListContact.setHasFixedSize(true)
        binding.rvListContact.adapter = listContactAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_contact_screen, container, false)

        runOnUI {

            // loi observe hoi lai tai
            mListContactViewModel.getIsEmptyStatus()
                .observe(viewLifecycleOwner, isEmptyStatusObserver)

            mListContactViewModel.getLoadingStatus()
                .observe(viewLifecycleOwner, loadingStatusObserver)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        mListContactViewModel.getData().removeObserver(listContactObserver)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mListContactViewModel = ViewModelProviders.of(this).get(ListContactViewModel::class.java)
        listContactAdapter = ListContactAdapter(context, this)


        lifecycleScope.launch {
            /*delay(250)*/
            mListContactViewModel.scan()
        }

        mListContactViewModel.getData().observe(this as LifecycleOwner, listContactObserver)          // loi observe hoi lai tai

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
                if (mListContactViewModel.searchContact(textChange.toString()).size <= 0) {
                    binding.clContact.visibility = View.GONE
                    binding.clNoContact.visibility = View.VISIBLE
                } else {
                    binding.clContact.visibility = View.VISIBLE
                    binding.clNoContact.visibility = View.GONE
                    listContactAdapter.submitList(mListContactViewModel.searchContact(textChange.toString()))
                }
            }
        })
    }

    override fun itemOnClick(phoneNumber: String, fileName: String) {
        hideKeyboard()
        viewStateManager.contactScreenOnItemClicked(this, phoneNumber, fileName)
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
}
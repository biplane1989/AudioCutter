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
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.databinding.MyStudioContactScreenBinding
import com.example.audiocutter.functions.mystudio.adapters.SetContactAdapter
import com.example.audiocutter.functions.mystudio.adapters.SetContactCallback
import com.example.audiocutter.functions.mystudio.objects.SetContactItemView
import com.example.audiocutter.functions.resultscreen.screens.ResultScreenArgs
import kotlinx.coroutines.launch

class SetContactScreen : BaseFragment(), SetContactCallback, View.OnClickListener {

    private lateinit var binding: MyStudioContactScreenBinding

    private val TAG = "giangtd4"
    lateinit var listContactAdapter: SetContactAdapter
    lateinit var mListContactViewModel: SetContactViewModel
    private val safeArg: SetContactScreenArgs by navArgs()      // truyen du lieu qua navigation

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
    val listContactObserver = Observer<List<SetContactItemView>> { listContact ->
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


    private val isSelectObserver = Observer<Boolean> { status ->
        if (status) {
            binding.ivOk.visibility = View.VISIBLE
            binding.tvOk.visibility = View.VISIBLE

            binding.ivNotOk.visibility = View.GONE
            binding.tvNotOk.visibility = View.GONE
        }
    }

    fun init() {
        binding.rvListContact.layoutManager = LinearLayoutManager(context)
        binding.rvListContact.setHasFixedSize(true)
        binding.rvListContact.adapter = listContactAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_contact_screen, container, false)

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
        listContactAdapter = SetContactAdapter(context, this)


//        lifecycleScope.launch {
        /*delay(250)*/
        mListContactViewModel.scan()
//        }

        mListContactViewModel.getData()
            .observe(this as LifecycleOwner, listContactObserver)          // loi observe hoi lai tai

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

            override fun onTextChanged(textChange: CharSequence, start: Int, before: Int, count: Int) {
                mListContactViewModel.searchContact(textChange.toString())
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
            binding.ivOk -> {
                if (mListContactViewModel.setRingtoneForContact(safeArg.pathUri)) {
                    Toast.makeText(context, getString(R.string.set_contact_ringtone_screen_set_ringtone_successfull), Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(context, getString(R.string.set_contact_ringtone_screen_set_ringtone_fail), Toast.LENGTH_SHORT)
                        .show()
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
}
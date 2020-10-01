package com.example.audiocutter.functions.contactscreen.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.manager.ContactManagerImpl
import kotlinx.android.synthetic.main.list_contact_screen.*


class ListContactScreen(mainCallBack: mainCallBack) : BaseFragment(), ContactCallback, View.OnClickListener {

    val callBack = mainCallBack

    // xin quyen
    val KEY = 1
    val CODE_WRITE_SETTINGS_PERMISSION = 2
    var isLoading = false   // trang thai load cua progressbar
    var currentView: View? = null
    val TAG = "giangtd4"
    lateinit var listContactAdapter: ListContactAdapter
    lateinit var mlistContactViewModel: ListContactViewModel
    private var listContact: LiveData<List<ContactItemView>>? = null

    // observer data
    val listContactObserver = Observer<List<ContactItemView>> { listContact ->
        if (listContact == null) {
        } else {
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
        currentView = inflater.inflate(R.layout.list_contact_screen, container, false)
        ContactManagerImpl.registerContentObserVerDeleted()
        listContact?.observe(this.viewLifecycleOwner, listContactObserver)
        return currentView
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mlistContactViewModel = ViewModelProviders.of(this)
            .get(ListContactViewModel(activity!!.application)::class.java)

        listContactAdapter = ListContactAdapter(context, this)
        isLoading = true
        runOnUI {
            listContact = mlistContactViewModel.getData() // get data from funtion newIntance
            listContact?.observe(this.viewLifecycleOwner, listContactObserver)

            isLoading = false

            currentView?.findViewById<ProgressBar>(R.id.pb_audio_cutter)?.visibility = View.GONE    // tai day hamonCreateView da chay xong r do runOnUI
        }
        requestPermission()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        iv_search.setOnClickListener(this)
        iv_search_close.setOnClickListener(this)
        iv_clear.setOnClickListener(this)

        if (isLoading) {
            pb_audio_cutter.visibility = View.VISIBLE
        } else {
            pb_audio_cutter.visibility = View.GONE
        }

        edt_search.addTextChangedListener(object : TextWatcher {
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
        callBack.item(phoneNumber, uri)
    }

    interface mainCallBack {
        fun item(phone: String, uri: String)
    }


    override fun onPostDestroy() {
        super.onPostDestroy()
        hideKeyboard()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_search -> {
                cl_default.visibility = View.GONE
                cl_search.visibility = View.VISIBLE
                showKeyboard()
            }
            R.id.iv_search_close -> {
                cl_default.visibility = View.VISIBLE
                cl_search.visibility = View.GONE
                edt_search.text.clear()
                hideKeyboard()
            }
            R.id.iv_clear -> {
                edt_search.text.clear()
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

    // xin quyen
    @SuppressLint("WrongConstant")
    private fun requestPermission() {
        val permission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context)
            if (PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.CHANGE_CONFIGURATION, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(requireActivity(), permissions, KEY)
            }
        } else {
            permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        }
        if (permission) {
            //do your code
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + requireContext().getPackageName())
                this.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_SETTINGS), CODE_WRITE_SETTINGS_PERMISSION)
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            Log.d("TAG", "MainActivity.CODE_WRITE_SETTINGS_PERMISSION success")
            if (Settings.System.canWrite(context)) {
//                setRingtone()
                Log.d(TAG, "onActivityResult: ")
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).setData(Uri.parse("package:" + activity?.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do your code
        }
    }
}
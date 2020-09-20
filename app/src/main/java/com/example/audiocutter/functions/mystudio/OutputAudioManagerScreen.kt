package com.example.audiocutter.functions.mystudio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialog
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialogListener
import com.example.audiocutter.functions.mystudio.fragment.MyStudioFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.my_studio_screen.*

class OutputAudioManagerScreen : BaseFragment(), DeleteDialogListener {

    val TAG = "giangtd"
    var isDeleteClicked = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_studio_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val myStudioAdapter = MyStudioViewPagerAdapter(baseActivity.supportFragmentManager)
        view_pager.adapter = myStudioAdapter

        tab_layout.setupWithViewPager(view_pager)

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Toast.makeText(baseActivity, "" + tab.position, Toast.LENGTH_SHORT).show()
                sendFragmentAction(
                    MyStudioFragment::class.java.name,
                    Constance.ACTION_STOP_MUSIC
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Toast.makeText(baseActivity, "" + tab.position, Toast.LENGTH_SHORT).show()
                sendFragmentAction(
                    MyStudioFragment::class.java.name,
                    Constance.ACTION_STOP_MUSIC
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                Toast.makeText(baseActivity, "" + tab.position, Toast.LENGTH_SHORT).show()
                sendFragmentAction(
                    MyStudioFragment::class.java.name,
                    Constance.ACTION_STOP_MUSIC
                )
            }
        })

        iv_extends.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.GONE
            cl_delete.visibility = View.VISIBLE

            sendFragmentAction(MyStudioFragment::class.java.name, Constance.ACTION_UNCHECK)
        })

        iv_close.setOnClickListener(View.OnClickListener {
            cl_default.visibility = View.VISIBLE
            cl_delete.visibility = View.GONE

            sendFragmentAction(MyStudioFragment::class.java.name, Constance.ACTION_HIDE)
        })


        iv_delete.setOnClickListener(View.OnClickListener {
            if (isDeleteClicked) {
                val dialog = DeleteDialog.newInstance(this, "giang")
                dialog.show(childFragmentManager, DeleteDialog.TAG)
                isDeleteClicked = false
            }

        })
    }

    override fun onDeleteClick() {
        Log.d(TAG, "onDeleteClick: ")
        isDeleteClicked = true
        sendFragmentAction(MyStudioFragment::class.java.name, Constance.ACTION_DELETE_ALL)
    }

    override fun onCancel() {
        isDeleteClicked = true
    }

}
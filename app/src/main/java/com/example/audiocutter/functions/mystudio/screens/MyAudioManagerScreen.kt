package com.example.audiocutter.functions.mystudio.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.databinding.MyStudioScreenBinding
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.MyStudioViewPagerAdapter
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialog
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialogListener
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.my_studio_screen.*


class MyAudioManagerScreen : BaseFragment(), DeleteDialogListener, View.OnClickListener {
    private lateinit var binding: MyStudioScreenBinding
    private val TAG = "giangtd"
    private var isDeleteClicked = true
    private var tabPosition = -1
    private lateinit var myAudioManagerViewModel: MyAudioManagerViewModel
    private val safeArg: MyAudioManagerScreenArgs by navArgs()


    private val actionObserver = Observer<ActionData> { it ->
        when (it.action) {
            Constance.ACTION_DELETE -> {  // nếu ko có item nào được chọn thì sẽ không hiển thị dialog delete
                if ((it.data == Constance.TRUE)) {
                    if (isDeleteClicked) {
                        val dialog = DeleteDialog.newInstance(this)
                        dialog.show(childFragmentManager, DeleteDialog.TAG)
                        isDeleteClicked = false
                    }
                } else {
                    Toast.makeText(context, getString(R.string.my_studio_notification_chose_item_delete), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_screen, container, false)

        myAudioManagerViewModel.getAction().observe(viewLifecycleOwner, actionObserver)

        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myAudioManagerViewModel = ViewModelProviders.of(this)
            .get(MyAudioManagerViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val myStudioAdapter = MyStudioViewPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = myStudioAdapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        tabPosition = binding.tabLayout.selectedTabPosition
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_STOP_MUSIC)
                tabPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        binding.tabLayout.getTabAt(requireArguments().getInt(Constance.TYPE_AUDIO_TO_NOTIFICATION))
            ?.select()

        binding.backButton.setOnClickListener(this)
        binding.ivExtends.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        binding.ivDelete.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivDelete -> {
                sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_CHECK_DELETE, tabPosition)
            }
            binding.ivClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clDelete.visibility = View.GONE

                // enable viewpayger swip
                binding.viewPager.setPagingEnabled(true)
                setEnabledTablayout(false)

                sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_HIDE, Constance.NO_TYPE_AUDIO)      // do action extends va close khong can quan tam toi data nen truyen vao -1
            }
            binding.ivExtends -> {
                binding.clDefault.visibility = View.GONE
                binding.clDelete.visibility = View.VISIBLE

                // disable viewpayger swip
                binding.viewPager.setPagingEnabled(false)
                setEnabledTablayout(true)

                sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_UNCHECK, Constance.NO_TYPE_AUDIO)
            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
        }
    }

    // enable thanh tiêu đề của tablayout
    @SuppressLint("ClickableViewAccessibility")
    fun setEnabledTablayout(isEnable: Boolean) {
        val tabLayout = binding.tabLayout.getChildAt(0) as LinearLayout
        for (i in 0 until tabLayout.childCount) {
            tabLayout.getChildAt(i).setOnTouchListener { v, event -> isEnable }
        }
    }

    override fun onDeleteClick(pathFolder: String) {
        isDeleteClicked = true
        sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_DELETE_ALL, tabPosition)
    }

    override fun onCancel() {
        isDeleteClicked = true
    }

    override fun getFragmentViewModel(): IViewModel? {          //TODO  truyen data giua cac fragment
        return myAudioManagerViewModel
    }

}
package com.example.audiocutter.functions.mystudio.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.base.IViewModel
import com.example.audiocutter.databinding.MyStudioScreenBinding
import com.example.audiocutter.functions.audiochooser.objects.SortAudioParam
import com.example.audiocutter.functions.common.SortAudioPopupWindow
import com.example.audiocutter.functions.common.SortField
import com.example.audiocutter.functions.common.SortType
import com.example.audiocutter.functions.common.SortValue
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.mystudio.adapters.MyStudioViewPagerAdapter
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialog
import com.example.audiocutter.functions.mystudio.dialog.DeleteDialogListener
import com.example.audiocutter.functions.mystudio.objects.ActionData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.my_studio_custom_header_tablayout_1.view.*


class MyAudioManagerScreen : BaseFragment(), DeleteDialogListener, View.OnClickListener {
    private lateinit var binding: MyStudioScreenBinding
    private var isDeleteClicked = true
    private var tabPositionSelected = -1
    private lateinit var myAudioManagerViewModel: MyAudioManagerViewModel



    private val actionObserver = Observer<ActionData?> { actionData ->
        actionData?.let {
            when (it.action) {
                Constance.ACTION_DELETE -> {  // nếu ko có item nào được chọn thì sẽ không hiển thị dialog delete
                    if (it.data == Constance.TRUE) {
                        if (isDeleteClicked) {
                            val dialog = DeleteDialog.newInstance(this)
                            dialog.show(childFragmentManager, DeleteDialog.TAG)
                            isDeleteClicked = false
                        }else{
                            // nothing
                        }
                    } else {
                        context?.let {
                            showNotification(getString(R.string.my_studio_notification_chose_item_delete))
                        }
                    }
                }
                else -> {
                    //Todo
                }
            }
        }
    }

    private fun showNotification(text: String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_studio_screen, container, false)

        return binding.root
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        myAudioManagerViewModel = ViewModelProviders.of(this)
            .get(MyAudioManagerViewModel::class.java)
    }

    override fun onCreateAnimation(
        transit: Int,
        enter: Boolean,
        nextAnim: Int
    ): Animation? {       // khi ket thuc animation chuyen man hinh thi moi cho dang ky observe
        if (nextAnim != 0x0) {
            val animator = AnimationUtils.loadAnimation(activity, nextAnim)

            animator.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (enter) {
                        setUp()
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {

                }
            })

            return animator
        } else {
            setUp()
        }
        return null
    }

    fun setTabLayoutPosition(typeAudio: Int) {
        binding.tabLayout.getTabAt(typeAudio)?.select()
    }

    fun setUp() {
        myAudioManagerViewModel.actionLiveData.observe(viewLifecycleOwner, actionObserver)

        val myStudioAdapter = MyStudioViewPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = myStudioAdapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.tabLayout.getTabAt(0)?.setCustomView(R.layout.my_studio_custom_header_tablayout_1)
        binding.tabLayout.getTabAt(1)?.setCustomView(R.layout.my_studio_custom_header_tablayout_2)
        binding.tabLayout.getTabAt(2)?.setCustomView(R.layout.my_studio_custom_header_tablayout_3)


        binding.tabLayout.getTabAt(requireArguments().getInt(Constance.TYPE_AUDIO_TO_NOTIFICATION))
            ?.select()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tabPositionSelected = binding.tabLayout.selectedTabPosition

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("ResourceAsColor")
            override fun onTabSelected(tab: TabLayout.Tab) {
                sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_STOP_MUSIC)
                tabPositionSelected = tab.position

                tab.view.tv_header?.let {
                    it.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.my_studio_header_select
                        )
                    )
                }

            }

            @SuppressLint("ResourceAsColor")
            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.tv_header?.let {
                    it.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.my_studio_header_unselect
                        )
                    )
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

//        binding.tabLayout.getTabAt(requireArguments().getInt(Constance.TYPE_AUDIO_TO_NOTIFICATION))
//            ?.select()

        binding.ivShare.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        binding.ivExtends.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        binding.ivDelete.setOnClickListener(this)
        binding.ivMyStudioScreenSort.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.ivShare -> {
                sendFragmentAction(
                    MyStudioScreen::class.java.name,
                    Constance.ACTION_SHARE,
                    tabPositionSelected
                )
            }
            binding.ivDelete -> {
                sendFragmentAction(
                    MyStudioScreen::class.java.name,
                    Constance.ACTION_CHECK_DELETE,
                    tabPositionSelected
                )
            }
            binding.ivClose -> {
                binding.clDefault.visibility = View.VISIBLE
                binding.clDelete.visibility = View.GONE

                // enable viewpayger swip
                binding.viewPager.setPagingEnabled(true)
                setEnabledTablayout(false)

                sendFragmentAction(
                    MyStudioScreen::class.java.name,
                    Constance.ACTION_HIDE,
                    tabPositionSelected
                )      // do action extends va close khong can quan tam toi data nen truyen vao -1
            }
            binding.ivExtends -> {              // trang thai isdelete
                binding.clDefault.visibility = View.GONE
                binding.clDelete.visibility = View.VISIBLE

                // disable viewpayger swip
                binding.viewPager.setPagingEnabled(false)
                setEnabledTablayout(true)

                sendFragmentAction(
                    MyStudioScreen::class.java.name,
                    Constance.ACTION_DELETE_STATUS,
                    tabPositionSelected
                )
            }
            binding.backButton -> {
                requireActivity().onBackPressed()
            }
            binding.ivMyStudioScreenSort -> {
                val sortAudioPopupWindow = SortAudioPopupWindow(
                    binding.ivMyStudioScreenSort , myAudioManagerViewModel.getSortValue(tabPositionSelected)) {
                    myAudioManagerViewModel.changeSortValue(tabPositionSelected, it)
                    sendFragmentAction(
                        MyStudioScreen::class.java.name,
                        Constance.ACTION_SORT_AUDIO,
                        SortAudioParam(tabPositionSelected, it)
                    )
                }
                sortAudioPopupWindow.show()
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

    override fun onDeleteClick(pathFolder: String) {    // delete dialog event
        binding.clDefault.visibility = View.VISIBLE
        binding.clDelete.visibility = View.GONE

        // enable viewpayger swip
        binding.viewPager.setPagingEnabled(true)
        setEnabledTablayout(false)

        isDeleteClicked = true
//        sendFragmentAction(MyStudioScreen::class.java.name, Constance.ACTION_HIDE, tabPosition)
        sendFragmentAction(
            MyStudioScreen::class.java.name,
            Constance.ACTION_DELETE_ALL,
            tabPositionSelected
        )
    }

    override fun onCancel() {
        isDeleteClicked = true
    }

    override fun getFragmentViewModel(): IViewModel? {          //TODO  truyen data giua cac fragment
        return myAudioManagerViewModel
    }

}
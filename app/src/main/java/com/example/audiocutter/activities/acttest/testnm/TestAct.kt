package com.example.audiocutter.activities.acttest.testnm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.act_test.*
import java.io.File


class TestAct : BaseActivity(), AppShareAdapter.AppShareListener {
    private lateinit var rvApp: RecyclerView
    private lateinit var listData: MutableList<ItemAppShareView>
    private lateinit var appShareAdapter: AppShareAdapter
    private var audioFile: AudioFile = AudioFile(
        File("/storage/emulated/0/AudioCutter/merger/vhkllllkj.mp3"),
        "hello",
        12588L,
        128,
        1255L,
        ManagerFactory.getAudioFileManager().getUriByPath(
            File("/storage/emulated/0/AudioCutter/merger/vhkllllkj.mp3")
        )!!,
        null,
        "",
        "",
        ""
    )
    private lateinit var cdlTest: CoordinatorLayout
    private lateinit var ivTest: RelativeLayout


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
        initList()
    }

    private fun initList() {
        appShareAdapter = AppShareAdapter(this)
        appShareAdapter.setOnCallBack(this)
        listData = getListItem()
        listData.add(
            ItemAppShareView(
                ItemAppShare(
                    "Show More",
                    resources.getDrawable(R.drawable.ic_more_app_share)
                ), true
            )
        )
        appShareAdapter.submitList(listData)
        rvApp.adapter = appShareAdapter
    }

    private fun getListItem(): MutableList<ItemAppShareView> {
        val tmp = mutableListOf<ItemAppShareView>()
        ManagerFactory.getAudioFileManager().getListApprQueryReceiveData().forEach {
            tmp.add(ItemAppShareView(it, false))
        }
        return tmp
    }


    private fun initViews() {
        rvApp = findViewById(R.id.rv_app_share)
        rvApp.layoutManager = LinearLayoutManager(this)
        ivTest = findViewById(R.id.rv_test_cdl)
        cdlTest = findViewById(R.id.cdl_test)



        bt_start.setOnClickListener {
            cdlTest.visibility = View.VISIBLE

            val bottomSheetBehavior = BottomSheetBehavior.from(ivTest)
            val mCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            }
            bottomSheetBehavior.addBottomSheetCallback(mCallBack)
        }

        iv_cancel_dialog.setOnClickListener {
            cdlTest.visibility = View.GONE
        }


        checkScrollRecycle()
    }

    private fun checkScrollRecycle() {
        rvApp.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val lnManager: LinearLayoutManager =
                        rvApp.layoutManager as LinearLayoutManager

                    val visibleItemCount = lnManager.childCount
                    val passVisibleItem = lnManager.findFirstCompletelyVisibleItemPosition()
                    val total = appShareAdapter.itemCount


                    if ((visibleItemCount + passVisibleItem) > total) {
                    }
                }
            }
        })
    }

    private fun shareFileAudio() {
        ManagerFactory.getAudioFileManager().shareFileAudio(audioFile)

    }


    override fun shareApp(position: Int) {
        if (!listData[position].isCheckButton) {
            val intent = Intent()
            intent.putExtra(Intent.EXTRA_STREAM, audioFile.uri)
            intent.type = "audio/*"
            intent.`package` = ManagerFactory.getAudioFileManager()
                .getListReceiveData()[position].activityInfo.packageName
            intent.action = Intent.ACTION_SEND
            startActivity(intent)
        } else {
            cdlTest.visibility = View.GONE
            shareFileAudio()
        }


    }


}




package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.functions.audiochooser.adapters.AppShareAdapter
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShareView

class DialogAppShare(val mContext: Context) : BaseDialog(), AppShareAdapter.AppShareListener {
    private lateinit var listData: MutableList<ItemAppShareView>
    private lateinit var listTmp: MutableList<ItemAppShare>
    private lateinit var rvApp: RecyclerView
    private lateinit var ivCancel: ImageView
    private lateinit var appShareAdapter: AppShareAdapter

    private lateinit var mCallBack: DialogAppListener

    fun setOnCallBack(event: DialogAppListener) {
        mCallBack = event
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)

    }

    override fun getLayoutResId(): Int {
        return R.layout.share_file_dialog
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        ivCancel = view.findViewById(R.id.iv_cancel_dialog)
        rvApp = view.findViewById(R.id.rv_app_share)
        rvApp.layoutManager = LinearLayoutManager(mContext)
        ivCancel.setOnClickListener {
            dismiss()
        }
        initList()

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
    }



    private fun initList() {
        appShareAdapter = AppShareAdapter(mContext)
        appShareAdapter.setOnCallBack(this)
        listData = mutableListOf()
        listTmp = ManagerFactory.getAudioFileManager().getListApprQueryReceiveData()
        listTmp.forEach { it ->
            listData.add(ItemAppShareView(it, false))
        }
        listData.add(
            ItemAppShareView(
                ItemAppShare(
                    "ShowMore",
                    mContext.resources.getDrawable(R.drawable.ic_more_app_share)
                ), true
            )
        )
        appShareAdapter.submitList(listData)

        rvApp.adapter = appShareAdapter
    }

    override fun shareApp(position: Int) {
        if (!listData[position].isCheckButton) {
            mCallBack.shareFilesToAppsDialog(position)
        } else {
            mCallBack.shareFileAudioToAppDevices()
        }
    }

    interface DialogAppListener {
        fun shareFileAudioToAppDevices()
        fun shareFilesToAppsDialog(position: Int)
    }

}
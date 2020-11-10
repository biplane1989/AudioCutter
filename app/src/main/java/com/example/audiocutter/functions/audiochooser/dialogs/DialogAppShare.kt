package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiochooser.adapters.AppShareAdapter
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShareView
import com.example.audiocutter.core.manager.ManagerFactory

class DialogAppShare(val mContext: Context) : DialogFragment(), AppShareAdapter.AppShareListener {
    private lateinit var listData: MutableList<ItemAppShareView>
    private lateinit var listTmp: MutableList<ItemAppShare>
    private lateinit var rvApp: RecyclerView
    private lateinit var ivCancel: ImageView
    private lateinit var rootView: View
    private lateinit var appShareAdapter: AppShareAdapter

    private lateinit var mCallBack: DialogAppListener

    fun setOnCallBack(event: DialogAppListener) {
        mCallBack = event
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTitle)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView =
            LayoutInflater.from(mContext).inflate(R.layout.share_file_dialog, container, false)
        isCancelable = false
        initView()
        initList()
        return rootView
    }


    private fun initView() {
        ivCancel = rootView.findViewById(R.id.iv_cancel_dialog)
        rvApp = rootView.findViewById(R.id.rv_app_share)
        rvApp.layoutManager = LinearLayoutManager(mContext)
        ivCancel.setOnClickListener {
            dismiss()
        }

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
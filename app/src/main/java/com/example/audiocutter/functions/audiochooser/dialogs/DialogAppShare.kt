package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.functions.audiochooser.adapters.AppShareAdapter
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShare
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShareView

enum class TypeShare {
    ONLYFILE,
    MULTIFILE
}

class DialogAppShare(
    val mContext: Context,
    private val listApps: List<ItemAppShare>,
    private val typeShare: TypeShare
) :
    BaseDialog(),
    AppShareAdapter.AppShareListener {
    private lateinit var listData: MutableList<ItemAppShareView>
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
        )
    }


    private fun initList() {
        appShareAdapter = AppShareAdapter(mContext)
        appShareAdapter.setOnCallBack(this)
        listData = mutableListOf()
        listApps.forEach { it ->
            listData.add(ItemAppShareView(it, false))
        }

        val btnMoreDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_more_app_share)
        btnMoreDrawable?.let {
            listData.add(
                ItemAppShareView(
                    ItemAppShare(
                        "ShowMore",
                        it,
                        mContext.packageName
                    ), true
                )
            )
        }

        appShareAdapter.submitList(listData)

        rvApp.adapter = appShareAdapter
    }

    override fun shareApp(position: Int) {
        if (!listData[position].isCheckButton) {
            mCallBack.shareFilesToAppsDialog(listApps[position].pkgName, typeShare)
        } else {
            if (typeShare == TypeShare.ONLYFILE) {
                mCallBack.shareFileAudioToAppDevices(TypeShare.ONLYFILE)
            } else {
                mCallBack.shareFileAudioToAppDevices(TypeShare.MULTIFILE)
            }
        }
    }

    interface DialogAppListener {
        fun shareFileAudioToAppDevices(multifile: TypeShare) {
        }


        fun shareFilesToAppsDialog(pkgName: String, typeShare: TypeShare) {

        }
    }

}
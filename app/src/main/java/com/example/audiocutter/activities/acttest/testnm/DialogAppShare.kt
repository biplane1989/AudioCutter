package com.example.audiocutter.activities.acttest.testnm

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import java.io.File

class DialogAppShare(val mContext: Context) : Dialog(mContext) {
    private lateinit var tvMore: TextView
    private lateinit var rvApp: RecyclerView
    private lateinit var appShareAdapter: AppShareAdapter
    private lateinit var audioFile: AudioFile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_share_file)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initView()
        initList()
    }

    private fun initList() {
        appShareAdapter = AppShareAdapter(mContext)
        val urlToShare = audioFile.uri
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "audio/*"

        intent.putExtra(Intent.EXTRA_STREAM, urlToShare)
        val listAppReceive = mContext.packageManager.queryIntentActivities(intent, 0)
        Log.d("TAG", "setIntent: ${listAppReceive.size}")

        val appNames: MutableList<ItemAppShare> = ArrayList()

        for (info in listAppReceive) {
            appNames.add(
                ItemAppShare(
                    info.loadLabel(mContext.packageManager).toString(),
                    info.loadIcon(mContext.packageManager) as BitmapDrawable
                )
            )
        }

//        appShareAdapter.submitList(appNames)
        rvApp.adapter = appShareAdapter
    }

    private fun initView() {
        tvMore = findViewById(R.id.tv_moreapp)
        rvApp = findViewById(R.id.rv_app_share)
        rvApp.layoutManager = GridLayoutManager(mContext,2)
        audioFile = AudioFile(
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
        tvMore.setOnClickListener {
            dismiss()
        }

    }
}
package com.example.audiocutter.activities.acttest.testnm

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseActivity
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.act_test.*
import java.io.File
import android.view.ViewGroup as ViewGroup1


class TestAct : BaseActivity() {
    private lateinit var rvApp: RecyclerView
    private lateinit var appShareAdapter: AppShareAdapter
    private lateinit var audioFile: AudioFile
    private val KEY_SHARE_AUDIO = "KEY_SHARE_AUDIO"
    private lateinit var cdlTest: CoordinatorLayout
    private lateinit var ivTest: RelativeLayout
    private lateinit var tvMoreApp: ImageView


    override fun createView(savedInstanceState: Bundle?) {
        setContentView(R.layout.act_test)
        initViews()
        initList()
    }

    private fun initList() {
        appShareAdapter = AppShareAdapter(this)
        val urlToShare = audioFile.uri
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "audio/*"

        intent.putExtra(Intent.EXTRA_STREAM, urlToShare)
        val listAppReceive = this.packageManager.queryIntentActivities(intent, 0)
        Log.d("TAG", "setIntent: ${listAppReceive.size}")

        val appNames: MutableList<DialogItem> = ArrayList()

        for (info in listAppReceive) {
            appNames.add(
                DialogItem(
                    info.loadLabel(this.packageManager).toString(),
                    info.loadIcon(this.packageManager)
                )
            )
        }

        appShareAdapter.submitList(appNames)
        rvApp.adapter = appShareAdapter
    }


    private fun initViews() {
        tvMoreApp = findViewById(R.id.tv_more_app)
        rvApp = findViewById(R.id.rv_app_share)
        rvApp.layoutManager = GridLayoutManager(this, 2)
        ivTest = findViewById(R.id.iv_test_cdl)
        cdlTest = findViewById(R.id.cdl_test)
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

        bt_start.setOnClickListener {
            cdlTest.visibility = View.VISIBLE

            val bottomSheetBehavior = BottomSheetBehavior.from(ivTest)
            val mCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    Log.d("TAG", "onSlide: $slideOffset")
                }

            }
            bottomSheetBehavior.addBottomSheetCallback(mCallBack)


        }

        tvMoreApp.setOnClickListener {
            cdlTest.visibility = View.GONE
        }
    }

    fun setIntent() {
        val urlToShare = audioFile.uri
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "audio/*"

        intent.putExtra(Intent.EXTRA_STREAM, urlToShare)

        val listAppReceive = packageManager.queryIntentActivities(intent, 0)
        Log.d("TAG", "setIntent: ${listAppReceive.size}")

        val appNames: MutableList<DialogItem> = ArrayList()

        for (info in listAppReceive) {
            appNames.add(
                DialogItem(
                    info.loadLabel(packageManager).toString(),
                    info.loadIcon(packageManager)
                )
            )
        }
        val newItem: List<DialogItem> = appNames
        val adapter: ListAdapter = object : ArrayAdapter<DialogItem?>(
            this,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            newItem
        ) {
            fun getView1(position: Int, convertView: View?, parent: ViewGroup1?): View? {
                //Use super class to create the View
                val itemView: View = super.getView(position, convertView, parent!!)
                val tv: TextView = itemView.findViewById(android.R.id.text1)
                tv.text = newItem[position].app
                tv.textSize = 15.0f
                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(newItem[position].icon, null, null, null)

                //Add margin between image and text (support various screen densities)
                val dp5 = (5 * resources.displayMetrics.density + 0.5f).toInt()
                tv.compoundDrawablePadding = dp5
                return itemView
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Custom Sharing Dialog")
        builder.setAdapter(adapter, DialogInterface.OnClickListener { dialog, item ->
            val info = listAppReceive[item]
            // start the selected activity
            Log.i("TAG", "Hi..hello. Intent is selected")
            intent.setPackage(info.activityInfo.packageName)
            startActivity(intent)

        })
        val dialog: AlertDialog = builder.create()
        dialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            "...",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        dialog.show()
    }



}




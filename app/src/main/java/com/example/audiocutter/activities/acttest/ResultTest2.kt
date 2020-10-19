package com.example.audiocutter.activities.acttest

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiocutter.R
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.functions.mystudio.adapters.ItemLoadingCallBack
import com.example.audiocutter.functions.mystudio.adapters.MyStudioAdapterItemLoading
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem
import kotlinx.android.synthetic.main.activity_result_test2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultTest2 : AppCompatActivity(), ItemLoadingCallBack {

    val TAG = "giangtd"
    val listConverting: ArrayList<ConvertingItem> = ArrayList()
    lateinit var loadingItemAdapter: MyStudioAdapterItemLoading

    val listLoadingObserver = Observer<List<ConvertingItem>> { listLoading ->

        listConverting.clear()
        for (item in listLoading) {
            listConverting.add(item)
        }
        listLoading?.let {
            if (!listLoading.isEmpty()) {
                loadingItemAdapter.submitList(ArrayList(listLoading))
            } else {
            }
        }
        Log.d(TAG, "listLoading size : " + listLoading.size)
    }


    private val progressObserver = Observer<ConvertingItem> {

        CoroutineScope(Dispatchers.Main).launch {
            val newItem = ConvertingItem(it.id, it.state, it.percent, it.audioFile)

            var index = 0
            if (!listConverting.isEmpty()) {
                for (item in listConverting) {
                    if (item.id == newItem.id) {
                        listConverting[index] = newItem
                    }
                    index++
                }
            }
            loadingItemAdapter.submitList(listConverting)
        }
        Log.d(TAG, "progressObserver percent: " + it.percent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_test2)

        loadingItemAdapter = MyStudioAdapterItemLoading(this)

        rv_loading_item_demo.layoutManager = LinearLayoutManager(this)
        rv_loading_item_demo.setHasFixedSize(true)
        rv_loading_item_demo.adapter = loadingItemAdapter

        ManagerFactory.getAudioEditorManager().getCurrentProcessingItem()
            .observe(this as LifecycleOwner, progressObserver)

        ManagerFactory.getAudioEditorManager().getListCuttingItems()
            .observe(this as LifecycleOwner, listLoadingObserver)

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)

    }

    override fun cancel(id: Int) {
        TODO("Not yet implemented")
    }
}
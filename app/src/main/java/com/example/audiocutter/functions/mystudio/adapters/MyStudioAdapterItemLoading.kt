package com.example.audiocutter.functions.mystudio.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.mystudio.Constance
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem


interface ItemLoadingCallBack {
    fun cancel(id: Int)
}

class MyStudioAdapterItemLoading(var itemloadingcallback: ItemLoadingCallBack) : ListAdapter<ConvertingItem, MyStudioAdapterItemLoading.ViewHolder>(ItemLoadingDiffCallBack()) {

    val TAG = "giangtd"
    private var listIteamLoading = ArrayList<ConvertingItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_item_loading, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val newItem = payloads.firstOrNull() as ConvertingItem
            val convertingItem = getItem(position)

            holder.pbLoading.max = 100
            holder.pbLoading.progress = convertingItem.percent

            holder.tvTitle.setText(convertingItem.audioFile.fileName)

            Log.d(TAG, "onBindViewHolder: ")

//            loadingItem.audioFile.bitmap?.let {
//                ivAvatar.setImageBitmap(it)
//            }
            holder.tvLoading.text = newItem.percent.toString() + "%"
        }
    }

    override fun submitList(list: List<ConvertingItem>?) {

        Log.d(TAG, "submitList: "+list?.size)
        if (list != null) {
            listIteamLoading = ArrayList(list)
            super.submitList(listIteamLoading)
        } else {
            listIteamLoading = ArrayList()
            super.submitList(listIteamLoading)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_music)
        val tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        val ivCancel: ImageView = itemView.findViewById(R.id.iv_cancel)


        fun bind() {
            val loadingItem = getItem(adapterPosition)
            pbLoading.max = 100
            pbLoading.progress = loadingItem.percent

            tvTitle.setText(loadingItem.audioFile.fileName)

//            loadingItem.audioFile.bitmap?.let {
//                ivAvatar.setImageBitmap(it)
//            }

            Log.d(TAG, "bind: ViewHolder")

            tvLoading.text = loadingItem.percent.toString() + "%"

            ivCancel.setOnClickListener(this)

        }

        override fun onClick(view: View) {
            val loadingItem = listIteamLoading.get(adapterPosition)
            when (view.id) {
                R.id.iv_cancel -> {
                    Log.d(TAG, "onClick: ")
                }
            }
        }
    }
}


class ItemLoadingDiffCallBack : DiffUtil.ItemCallback<ConvertingItem>() {

    val TAG = "giangtd"
    override fun areItemsTheSame(oldItemView: ConvertingItem, newItemView: ConvertingItem): Boolean {
//        Log.d(TAG, "areItemsTheSame: ")
        return oldItemView.id == newItemView.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItemView: ConvertingItem, newItemView: ConvertingItem): Boolean {
//        Log.d(TAG, "areItemsTheSame: 2")

        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: ConvertingItem, newItem: ConvertingItem): Any? {
//        Log.d(TAG, "getChangePayload: ")
        return newItem
    }
}
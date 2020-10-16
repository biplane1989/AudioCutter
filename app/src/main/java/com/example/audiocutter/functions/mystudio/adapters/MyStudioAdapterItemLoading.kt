package com.example.audiocutter.functions.mystudio.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.audiocutter.R
import com.example.audiocutter.functions.contacts.adapters.ListSelectAdapter
import com.example.audiocutter.functions.contacts.adapters.SelectAudioDiffCallBack
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.functions.resultscreen.objects.ConvertingItem


interface ItemLoadingCallBack {
    fun cancel(id: Int)
}

class MyStudioAdapterItemLoading(var itemloadingcallback: ItemLoadingCallBack) : ListAdapter<ConvertingItem, ListSelectAdapter.ViewHolder>(ItemLoadingDiffCallBack()) {

    private var listIteamLoading = ArrayList<ConvertingItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSelectAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_studio_item_loading, parent, false)
        return ListSelectAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListSelectAdapter.ViewHolder, position: Int) {
        holder.bind()
    }

    override fun submitList(list: List<ConvertingItem>?) {

        if (list != null) {
            listIteamLoading = ArrayList(list)
            super.submitList(listIteamLoading)
        } else {
            listIteamLoading = ArrayList()
            super.submitList(listIteamLoading)
        }
    }

}


class ItemLoadingDiffCallBack : DiffUtil.ItemCallback<ConvertingItem>() {

    override fun areItemsTheSame(oldItemView: ConvertingItem, newItemView: ConvertingItem): Boolean {
        return oldItemView.percent == newItemView.percent
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItemView: ConvertingItem, newItemView: ConvertingItem): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: ConvertingItem, newItem: ConvertingItem): Any? {
        return newItem
    }
}
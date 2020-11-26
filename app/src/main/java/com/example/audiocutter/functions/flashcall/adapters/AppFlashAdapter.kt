package com.example.audiocutter.functions.flashcall.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.core.manager.AppFlashItem

class AppFlashAdapter(val mContext: Context) : ListAdapter<AppFlashItem, AppFlashAdapter.AppFlashHolder>(AppFlashDiff()) {
    private lateinit var mCallBack: AppFlashListener

    fun setOnCallBack(event: AppFlashListener) {
        mCallBack = event
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppFlashHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_app_chooser_flashcall, parent, false)
        return AppFlashHolder(view)
    }

    override fun onBindViewHolder(holder: AppFlashHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (payloads.isEmpty()) {
            Log.e("TAG", "onBindViewHolder: in payloads isempty")
            onBindViewHolder(holder, position)
        } else {
            Log.e("TAG", "onBindViewHolder: in payloads isnotempty")
            when (item.selected) {
                true -> {
                    holder.swApp.isChecked = true
                }
                false -> {
                    holder.swApp.isChecked = false
                }
            }
        }
    }

    override fun onBindViewHolder(holder: AppFlashHolder, position: Int) {
        holder.onBind()
    }

    inner class AppFlashHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivApp = itemView.findViewById<ImageView>(R.id.iv_icon_app_flash)
        var tvApp = itemView.findViewById<TextView>(R.id.tv_name_app_flash)
        var swApp = itemView.findViewById<SwitchCompat>(R.id.sw_chooser_app)


        fun onBind() {
            Log.e("TAG", "onBindViewHolder:in onBind")
            val item = getItem(adapterPosition)
            item.icon?.let {
                ivApp.setImageBitmap(item.icon)
            }
            tvApp.text = item.name
            swApp.setOnCheckedChangeListener(null)
            when (item.selected) {
                true -> {
                    swApp.isChecked = true
                }
                false -> {
                    swApp.isChecked = false
                }
            }
            swApp.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mCallBack.enableFlashForApp(item)
                } else {
                    mCallBack.disableFlashForApp(item)
                }
            }
            itemView.setOnClickListener {
                swApp.isChecked = !swApp.isChecked
                if (swApp.isChecked) {
                    mCallBack.enableFlashForApp(item)
                } else {
                    mCallBack.disableFlashForApp(item)
                }
            }
        }

    }

    interface AppFlashListener {
        fun enableFlashForApp(appItem: AppFlashItem)
        fun disableFlashForApp(appItem: AppFlashItem)
    }
}

class AppFlashDiff() : DiffUtil.ItemCallback<AppFlashItem>() {
    override fun areItemsTheSame(oldItem: AppFlashItem, newItem: AppFlashItem): Boolean {
        return oldItem.pkgName == newItem.pkgName
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AppFlashItem, newItem: AppFlashItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: AppFlashItem, newItem: AppFlashItem): Any {
        return newItem
    }


}

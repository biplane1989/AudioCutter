package com.example.audiocutter.functions.flashcall.adapters

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.audiocutter.functions.flashcall.`object`.AppChooserView

class AppFlashAdapter(val mContext: Context) : ListAdapter<AppChooserView, AppFlashAdapter.AppFlashHolder>(AppFlashDiff()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppFlashHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_app_chooser_flashcall, parent, false)
        return AppFlashHolder(view)
    }

    override fun onBindViewHolder(holder: AppFlashHolder, position: Int) {
        val item = getItem(position)
        holder.ivApp.setImageDrawable(item.app.icon)
        holder.tvApp.text = item.app.name
        when (item.isChecked) {
            true -> {
                holder.swApp.isChecked = true
            }
            false -> {
                holder.swApp.isChecked = false
            }
        }
    }

    inner class AppFlashHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivApp = itemView.findViewById<ImageView>(R.id.iv_icon_app_flash)
        var tvApp = itemView.findViewById<TextView>(R.id.tv_name_app_flash)
        var swApp = itemView.findViewById<SwitchCompat>(R.id.sw_chooser_app)
    }
}

class AppFlashDiff() : DiffUtil.ItemCallback<AppChooserView>() {
    override fun areItemsTheSame(oldItem: AppChooserView, newItem: AppChooserView): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AppChooserView, newItem: AppChooserView): Boolean {
        return oldItem == newItem
    }
}

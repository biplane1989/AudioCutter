package com.example.audiocutter.activities.acttest.testnm

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R

class AppShareAdapter(val mContext: Context) :
    ListAdapter<DialogItem, AppShareAdapter.AppShareHolder>(AppShareDiff()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShareHolder {
        val itemView =
            LayoutInflater.from(mContext).inflate(R.layout.item_share_file, parent, false)
        return AppShareHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppShareHolder, position: Int) {
        val item = getItem(position)

        holder.ivApp.setImageDrawable(item.icon)

    }

    inner class AppShareHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val ivApp = itemView.findViewById<ImageView>(R.id.iv_icon_app)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {

        }
    }
}

class AppShareDiff : DiffUtil.ItemCallback<DialogItem>() {
    override fun areItemsTheSame(oldItem: DialogItem, newItem: DialogItem): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DialogItem, newItem: DialogItem): Boolean {
        return oldItem == newItem
    }

}
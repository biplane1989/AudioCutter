package com.example.audiocutter.functions.audiochooser.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiochooser.objects.ItemAppShareView

class AppShareAdapter(val mContext: Context) :
    ListAdapter<ItemAppShareView, AppShareAdapter.AppShareHolder>(AppShareDiff()) {
    private lateinit var mCallBack: AppShareListener

    fun setOnCallBack(event: AppShareListener) {
        mCallBack = event
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShareHolder {
        val itemView =
            LayoutInflater.from(mContext).inflate(R.layout.item_share_file, parent, false)
        return AppShareHolder(itemView)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: AppShareHolder, position: Int) {
        val item = getItem(position)
        if (!item.isCheckButton) {
            holder.ivApp.setImageDrawable(item.itemAppShare.icon)
            holder.tvApp.text = item.itemAppShare.app
        } else {
            holder.ivApp.setImageDrawable(item.itemAppShare.icon)
            holder.tvApp.text = item.itemAppShare.app
            holder.tvApp.setTextColor(ContextCompat.getColor(mContext, R.color.colorgray))
        }


    }


    inner class AppShareHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val ivApp = itemView.findViewById<ImageView>(R.id.iv_icon_app_flash)
        val tvApp = itemView.findViewById<TextView>(R.id.tv_app_share)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mCallBack.shareApp(adapterPosition)
        }
    }

    interface AppShareListener {
        fun shareApp(position: Int)
    }
}

class AppShareDiff : DiffUtil.ItemCallback<ItemAppShareView>() {
    override fun areItemsTheSame(oldItem: ItemAppShareView, newItem: ItemAppShareView): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ItemAppShareView, newItem: ItemAppShareView): Boolean {
        return oldItem == newItem
    }

}
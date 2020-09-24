package com.example.audiocutter.functions.contactscreen.contacts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.functions.contactscreen.ContactItemView
import java.util.ArrayList

class ListContactAdapter(context: Context?) : ListAdapter<ContactItemView, RecyclerView.ViewHolder>(ContactDiffCallBack()) {

    var mListContact: ArrayList<ContactItemView> = ArrayList()

    var mContext: Context? = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SECTION_VIEW) {
            HeaderViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.list_contact_item_header, parent, false))
        } else ItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact_item_content, parent, false), mContext)
    }

    override fun getItemViewType(position: Int): Int {
        return if (mListContact[position].isHeader) {
            SECTION_VIEW
        } else {
            CONTENT_VIEW
        }
    }

    override fun submitList(list: List<ContactItemView>?) {
        if (list != null) {
            mListContact = ArrayList(list)
            super.submitList(mListContact)
        } else {
            mListContact = ArrayList()
            super.submitList(mListContact)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mContext == null) {
            return
        }
        if (SECTION_VIEW == getItemViewType(position)) {
            val headerViewHolder = holder as HeaderViewHolder
            headerViewHolder.onBind()
        } else {
            val itemViewHolder = holder as ItemViewHolder
            itemViewHolder.onBind()
        }
    }

    override fun getItemCount(): Int {
        return mListContact.size
    }

    inner class ItemViewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val ringtone: TextView = itemView.findViewById(R.id.tv_ringtone)
        val ringtoneDefault: TextView = itemView.findViewById(R.id.tv_ringtone_default)

        fun onBind() {
            val contentItem = getItem(adapterPosition)
            tvName.text = contentItem.contactItem.name
            val avatar = getImageCover(contentItem.contactItem.thumb)

            if (avatar != null) {
                ivAvatar.setImageBitmap(avatar)
            }
        }

    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeader: TextView = itemView.findViewById(R.id.tv_header)

        fun onBind() {
            val headerItem = getItem(adapterPosition)
            tvHeader.text = headerItem.contactItem.name.get(0).toUpperCase().toString()
        }
    }

    companion object {
        const val SECTION_VIEW = 0
        const val CONTENT_VIEW = 1
    }

    fun getImageCover(path: String?): Bitmap? {
        try {
            if (path != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(mContext?.contentResolver, Uri.parse(path))
                return bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}

class ContactDiffCallBack : DiffUtil.ItemCallback<ContactItemView>() {

    override fun areItemsTheSame(oldItemView: ContactItemView, newItemView: ContactItemView): Boolean {
        return oldItemView.contactItem.phoneNumber == newItemView.contactItem.phoneNumber
    }

    override fun areContentsTheSame(oldItemView: ContactItemView, newItemView: ContactItemView): Boolean {
        return oldItemView == newItemView
    }

    override fun getChangePayload(oldItem: ContactItemView, newItem: ContactItemView): Any? {

        return newItem
    }
}
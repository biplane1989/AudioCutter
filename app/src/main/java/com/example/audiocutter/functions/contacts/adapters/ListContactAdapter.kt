package com.example.audiocutter.functions.contacts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.audiocutter.R
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.util.Utils
import java.util.*


interface ContactCallback {

    fun itemOnClick(phoneNumber: String, ringtonePath: String)
}

class ListContactAdapter(context: Context?, var contactCallback: ContactCallback) : ListAdapter<ContactItemView, RecyclerView.ViewHolder>(ContactDiffCallBack()) {

    var mContext: Context? = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SECTION_VIEW) {
            HeaderViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.list_contact_item_header, parent, false))
        } else ItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact_item_content, parent, false), mContext)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader) {
            SECTION_VIEW
        } else {
            CONTENT_VIEW
        }
    }

    override fun submitList(list: List<ContactItemView>?) {
        if (list != null) {
            super.submitList(ArrayList(list))
        } else {
            super.submitList(null)
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

    // khi chi thay doi 1 truong trong data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {

            if (mContext == null) {
                return
            }
            if (holder is HeaderViewHolder) {
                val headerViewHolder = holder as HeaderViewHolder
                headerViewHolder.onBind()
            } else {
                val itemViewHolder = holder as ItemViewHolder
                val newItem = payloads.firstOrNull() as ContactItemView

//                newItem.contactItem.thumb?.let {
//                    Glide.with(mContext!!).load(it)
//                        .transform(RoundedCorners(Utils.convertDp2Px(4, mContext!!).toInt()))
//                        .into(itemViewHolder.ivAvatar)
//                }

                if (!newItem.contactItem.isRingtoneDefault) {
                    itemViewHolder.tvRingtoneDefault.visibility = View.GONE
                    itemViewHolder.cvDefault.visibility = View.GONE
                    itemViewHolder.tvRingtone.visibility = View.VISIBLE
                    itemViewHolder.tvRingtone.text = newItem.contactItem.fileNameRingtone.toLowerCase(Locale.ROOT)  // get name song by uri    | sua contactItem = newItem
                } else {
                    itemViewHolder.tvRingtoneDefault.visibility = View.VISIBLE
                    itemViewHolder.cvDefault.visibility = View.VISIBLE
                    itemViewHolder.tvRingtone.visibility = View.GONE
                    itemViewHolder.tvRingtoneDefault.text = newItem.contactItem.fileNameRingtone.toLowerCase(Locale.ROOT)

                }
            }
        }
    }

    inner class ItemViewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvRingtone: TextView = itemView.findViewById(R.id.tv_ringtone)
        val tvRingtoneDefault: TextView = itemView.findViewById(R.id.tv_ringtone_default)
        val cvDefault: CardView = itemView.findViewById(R.id.cv_default)
        val clItemContact: ConstraintLayout = itemView.findViewById(R.id.cl_item_contact)

        fun onBind() {
            val contentItem = getItem(adapterPosition)

            tvName.text = contentItem.contactItem.name

            if (contentItem.contactItem.thumb != null) {
                Glide.with(itemView).load(contentItem.contactItem.thumb)
                    .transform(RoundedCorners(Utils.convertDp2Px(4, itemView.context).toInt()))
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.list_contact_item_avatar)
            }

            if (!contentItem.contactItem.isRingtoneDefault) {

                tvRingtoneDefault.visibility = View.GONE
                cvDefault.visibility = View.GONE
                tvRingtone.visibility = View.VISIBLE
                tvRingtone.text = contentItem.contactItem.fileNameRingtone.toLowerCase(Locale.ROOT)
            } else {
                tvRingtoneDefault.visibility = View.VISIBLE
                cvDefault.visibility = View.VISIBLE
                tvRingtone.visibility = View.GONE
                tvRingtoneDefault.text = contentItem.contactItem.fileNameRingtone.toLowerCase(Locale.ROOT)
            }

            clItemContact.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.cl_item_contact -> {
                    val contactItemView = getItem(adapterPosition)
                    contactItemView.contactItem.ringtone?.let {
                        if (it != "") {
                            contactCallback.itemOnClick(contactItemView.contactItem.phoneNumber, it)
                        }
                    }
                }
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeader: TextView = itemView.findViewById(R.id.tv_header)

        fun onBind() {
            val headerItem = getItem(adapterPosition)
            tvHeader.text = headerItem.contactHeader.get(0).toUpperCase().toString()
        }
    }

    companion object {
        const val SECTION_VIEW = 0
        const val CONTENT_VIEW = 1
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
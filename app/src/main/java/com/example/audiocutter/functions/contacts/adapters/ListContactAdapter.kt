package com.example.audiocutter.functions.contacts.adapters

import android.content.Context
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
import com.example.audiocutter.R
import com.example.audiocutter.functions.contacts.objects.ContactItemView
import com.example.audiocutter.functions.contacts.objects.SelectItemView
import com.example.audiocutter.util.Utils
import java.util.*


interface ContactCallback {

    fun itemOnClick(phoneNumber: String, fileName: String)
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
//                val contactItem = getItem(position)

//                if (contactItem.contactItem.ringtone != null) {
                if (!newItem.contactItem.isRingtoneDefault) {
                    itemViewHolder.tvRingtoneDefault.visibility = View.GONE
                    itemViewHolder.cvDefault.visibility = View.GONE
                    itemViewHolder.tvRingtone.visibility = View.VISIBLE

//                    val fileName = Utils.getNameByUri(mContext!!, newItem.contactItem.ringtone!!)   // get name song by uri    | sua contactItem = newItem
//                    itemViewHolder.tvRingtone.text = fileName.toLowerCase(Locale.ROOT)
                    itemViewHolder.tvRingtone.text = newItem.contactItem.fileNameRingtone?.toLowerCase(Locale.ROOT)  // get name song by uri    | sua contactItem = newItem

                } else {
                    itemViewHolder.tvRingtoneDefault.visibility = View.VISIBLE
                    itemViewHolder.cvDefault.visibility = View.VISIBLE
                    itemViewHolder.tvRingtone.visibility = View.GONE

//                    if (Utils.getUriRingtoneDefault(mContext!!) != null) {
//                        val fileName = Utils.getNameByUri(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
//                            .toString())
//                        itemViewHolder.tvRingtoneDefault.text = fileName.toLowerCase(Locale.ROOT)
//                    }

                    itemViewHolder.tvRingtoneDefault.text = newItem.fileNameRingtoneDefault?.toLowerCase(Locale.ROOT)

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

//            val avatar = Utils.getImageCover(mContext!!, contentItem.contactItem.thumb)
//
//            if (avatar != null) {
//                ivAvatar.setImageBitmap(avatar)
//            }

            contentItem.avatar?.let {
                ivAvatar.setImageBitmap(contentItem.avatar)
            }

//            if (contentItem.contactItem.ringtone != null) {
            if (!contentItem.contactItem.isRingtoneDefault) {

                tvRingtoneDefault.visibility = View.GONE
                cvDefault.visibility = View.GONE
                tvRingtone.visibility = View.VISIBLE

//                val fileName = Utils.getNameByUri(mContext!!, contentItem.contactItem.ringtone!!)   // get name song by uri
//                tvRingtone.text = fileName.toLowerCase(Locale.ROOT)
                tvRingtone.text = contentItem.contactItem.fileNameRingtone?.toLowerCase(Locale.ROOT)
            } else {
                tvRingtoneDefault.visibility = View.VISIBLE
                cvDefault.visibility = View.VISIBLE
                tvRingtone.visibility = View.GONE

//                if (Utils.getUriRingtoneDefault(mContext!!) != null) {
//                    val fileName = Utils.getNameByUri(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
//                        .toString())
//
//                    tvRingtoneDefault.text = fileName.toLowerCase(Locale.ROOT)
//                }

                tvRingtoneDefault.text = contentItem.fileNameRingtoneDefault?.toLowerCase(Locale.ROOT)
            }

            clItemContact.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.cl_item_contact -> {
//                    val fileName: String
//                    if (getItem(adapterPosition).contactItem.ringtone != null) {
//                        fileName = Utils.getNameByUri(mContext!!, getItem(adapterPosition).contactItem.ringtone!!)
//                    } else {
//                        fileName = Utils.getNameByUri(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
//                            .toString())
//                    }

                    val contactItemView = getItem(adapterPosition)
                    contactItemView.contactItem.fileNameRingtone?.let {
                        contactCallback.itemOnClick(contactItemView.contactItem.phoneNumber, it)
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
package com.example.audiocutter.functions.contactscreen.contacts

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
import com.example.audiocutter.util.Utils
import java.util.*


interface ContactCallback {

    fun itemOnClick(phoneNumber: String, uri: String)
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
                val ringtone = payloads.firstOrNull() as String
                val contactItem = getItem(position)

               /* if (ringtone != null) {
                    itemViewHolder.tvRingtoneDefault.visibility = View.GONE
                    itemViewHolder.cvDefault.visibility = View.GONE
                    itemViewHolder.tvRingtone.visibility = View.VISIBLE

                    val contactInfomation = Utils.getPlayList(mContext!!, contactItem.contactItem.ringtone!!)   // get name song by uri
                    itemViewHolder.tvRingtone.text = contactInfomation.title
                } else {
                    itemViewHolder.tvRingtoneDefault.visibility = View.VISIBLE
                    itemViewHolder.cvDefault.visibility = View.VISIBLE
                    itemViewHolder.tvRingtone.visibility = View.GONE

                    val contactInfomation = Utils.getPlayList(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
                        .toString())
                    itemViewHolder.tvRingtoneDefault.text = contactInfomation.title
                }*/
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
            val avatar = Utils.getImageCover(mContext!!, contentItem.contactItem.thumb)

            if (avatar != null) {
                ivAvatar.setImageBitmap(avatar)
            }
           /* if (contentItem.contactItem.ringtone != null) {

                tvRingtoneDefault.visibility = View.GONE
                cvDefault.visibility = View.GONE
                tvRingtone.visibility = View.VISIBLE

                val contactInfomation = Utils.getPlayList(mContext!!, contentItem.contactItem.ringtone!!)   // get name song by uri
                tvRingtone.text = contactInfomation.title
            } else {
                tvRingtoneDefault.visibility = View.VISIBLE
                cvDefault.visibility = View.VISIBLE
                tvRingtone.visibility = View.GONE

                val contactInfomation = Utils.getPlayList(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
                    .toString())
                tvRingtoneDefault.text = contactInfomation.title
            }*/

            clItemContact.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.cl_item_contact -> {
                    val contactInfomation: ContactInfomation
                    if (getItem(adapterPosition).contactItem.ringtone != null) {
                        contactInfomation = Utils.getPlayList(mContext!!, getItem(adapterPosition).contactItem.ringtone!!)
                    } else {
                        contactInfomation = Utils.getPlayList(mContext!!, Utils.getUriRingtoneDefault(mContext!!)
                            .toString())
                    }
                    contactCallback.itemOnClick(getItem(adapterPosition).contactItem.phoneNumber, contactInfomation.fileName)
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

        return newItem.contactItem.ringtone
    }
}
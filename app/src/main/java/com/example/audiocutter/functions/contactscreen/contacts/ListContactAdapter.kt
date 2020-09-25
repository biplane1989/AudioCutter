package com.example.audiocutter.functions.contactscreen.contacts

import android.content.Context
import android.preference.PreferenceActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils
import java.util.*


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

                if (ringtone != null) {
                    itemViewHolder.tvRingtoneDefault.visibility = View.GONE
                    itemViewHolder.cvDefault.visibility = View.GONE
                    itemViewHolder.tvRingtone.visibility = View.VISIBLE

                    val ringtoneTitle = Utils.getPlayList(mContext!!, contactItem.contactItem.ringtone!!)   // get name song by uri
                    itemViewHolder.tvRingtone.text = ringtoneTitle
                } else {
                    itemViewHolder.tvRingtoneDefault.visibility = View.VISIBLE
                    itemViewHolder.cvDefault.visibility = View.VISIBLE
                    itemViewHolder.tvRingtone.visibility = View.GONE

                    val ringtoneDefault = Utils.getPlayList(mContext!!, Utils.getCurrentSound(mContext!!)
                        .toString())
                    itemViewHolder.tvRingtoneDefault.text = ringtoneDefault
                }
//            }
            }
        }
    }

    override fun getItemCount(): Int {
        return mListContact.size
    }

    inner class ItemViewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvRingtone: TextView = itemView.findViewById(R.id.tv_ringtone)
        val tvRingtoneDefault: TextView = itemView.findViewById(R.id.tv_ringtone_default)
        val cvDefault: CardView = itemView.findViewById(R.id.cv_default)

        fun onBind() {
            val contentItem = getItem(adapterPosition)
            tvName.text = contentItem.contactItem.name
            val avatar = Utils.getImageCover(mContext!!, contentItem.contactItem.thumb)

            if (avatar != null) {
                ivAvatar.setImageBitmap(avatar)
            }
            if (contentItem.contactItem.ringtone != null) {

                tvRingtoneDefault.visibility = View.GONE
                cvDefault.visibility = View.GONE
                tvRingtone.visibility = View.VISIBLE

                val ringtoneTitle = Utils.getPlayList(mContext!!, contentItem.contactItem.ringtone!!)   // get name song by uri
                tvRingtone.text = ringtoneTitle
            } else {
                tvRingtoneDefault.visibility = View.VISIBLE
                cvDefault.visibility = View.VISIBLE
                tvRingtone.visibility = View.GONE

                val ringtoneDefault = Utils.getPlayList(mContext!!, Utils.getCurrentSound(mContext!!)
                    .toString())
                tvRingtoneDefault.text = ringtoneDefault
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
package com.example.audiocutter.ui.audiochooser.merge

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiochooser.event.OnItemTouchHelper

class MyItemTouchHelper(private var mAdapter : OnItemTouchHelper, val context: Context) :
    ItemTouchHelper.Callback() {
    private var mAdapterTouch: OnItemTouchHelper = mAdapter
    private var isDragging = false


    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.setBackgroundColor(
            ContextCompat.getColor(
                viewHolder.itemView.context,
                R.color.colorWhiteAlpha
            )
        )
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        isDragging = actionState != ItemTouchHelper.ACTION_STATE_IDLE
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder!!.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorYelowMyTouchHelper
                )
            )
        }
    }


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapterTouch.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //do no thing

    }

    fun isDragging():Boolean{
        return isDragging
    }

}
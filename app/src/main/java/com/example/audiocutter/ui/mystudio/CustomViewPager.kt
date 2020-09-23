package com.example.audiocutter.ui.mystudio

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager


class CustomViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    private var isPagingEnabled = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return isPagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(b: Boolean) {
        isPagingEnabled = b
    }
}
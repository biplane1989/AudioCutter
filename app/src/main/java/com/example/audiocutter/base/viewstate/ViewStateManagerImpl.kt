package com.example.a0025antivirusapplockclean.base.viewstate

import android.util.Log
import java.lang.StringBuilder


object ViewStateManagerImpl : ViewStateManager, ViewStateMutable {
    private var isWaitingForFinishingScreen = false
    private val viewStateList: MutableList<ViewStateScreen> = mutableListOf()
    override fun initState(viewStateScreen: ViewStateScreen) {
        viewStateList.clear()
        viewStateList.add(viewStateScreen)
    }

    override fun getLastState(): ViewStateScreen? {
        return if (viewStateList.size == 0) {
            null
        } else viewStateList[viewStateList.size - 1]
    }

    override fun getPrevState(): ViewStateScreen? {
        return if (viewStateList.size < 2) {
            null
        } else viewStateList[viewStateList.size - 2]
    }

    override fun popViewState() {
        if (viewStateList.size > 0) {
            viewStateList.remove(getLastState())
        }
        logState()
    }

    override fun pushViewState(viewStateScreen: ViewStateScreen) {
        viewStateList.add(viewStateScreen)
        logState()
    }

    override fun existViewState(viewStateScreen: ViewStateScreen): Boolean {
        return viewStateList.contains(viewStateScreen)
    }

    override fun onBackPressed(): Boolean {
        if (isWaitingForFinishingScreen) {
            return false
        }
        isWaitingForFinishingScreen = true
        popViewState()
        logState()
        return true
    }

    override fun onScreenFinished() {
        isWaitingForFinishingScreen = false
    }

    override fun getViewStateMutable(): ViewStateMutable {
        return this
    }

    private fun logState() {
        val str = StringBuilder()
        viewStateList.forEach {
            str.append(it.name).append("->")
        }
        Log.d("ViewStateManagerImpl", str.toString() + " ${viewStateList.size}")
    }

}
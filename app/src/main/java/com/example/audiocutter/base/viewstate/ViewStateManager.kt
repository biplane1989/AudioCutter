package com.example.a0025antivirusapplockclean.base.viewstate

interface ViewStateManager {
    fun getViewStateMutable(): ViewStateMutable
    fun initState(viewStateScreen: ViewStateScreen)
    fun onBackPressed()
}
package com.example.a0025antivirusapplockclean.base.viewstate


interface ViewStateMutable {
    fun getLastState(): ViewStateScreen?
    fun getPrevState(): ViewStateScreen?
    fun popViewState()
    fun popViewStateTo(viewStateScreen: ViewStateScreen)
    fun pushViewState(viewStateScreen: ViewStateScreen)
    fun existViewState(viewStateScreen: ViewStateScreen): Boolean

}
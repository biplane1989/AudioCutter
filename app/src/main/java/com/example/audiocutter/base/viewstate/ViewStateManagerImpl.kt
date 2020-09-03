package com.example.a0025antivirusapplockclean.base.viewstate


object ViewStateManagerImpl : ViewStateManager, ViewStateMutable{
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
    }

    override fun pushViewState(viewStateScreen: ViewStateScreen) {
        viewStateList.add(viewStateScreen)
    }

    override fun existViewState(viewStateScreen: ViewStateScreen): Boolean {
        return viewStateList.contains(viewStateScreen)
    }

    override fun onBackPressed() {
        popViewState()
    }

    override fun getViewStateMutable(): ViewStateMutable {
       return this
    }
}
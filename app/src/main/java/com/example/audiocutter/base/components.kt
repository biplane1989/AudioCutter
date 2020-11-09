package com.example.audiocutter.base

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateManager
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateManagerImpl
import com.example.audiocutter.functions.mystudio.screens.FragmentMeta
import com.example.audiocutter.functions.mystudio.screens.IMyStudioActivity
import kotlinx.coroutines.*

private const val DIALOG_STYLE_KEY = "DIALOG_STYLE_KEY"

typealias DialogConfirmListener = (action: String) -> Unit
typealias Executable = suspend () -> Unit
typealias ExecutableForResult<T> = suspend () -> (T)

abstract class BaseActivity : AppCompatActivity() {

    protected val viewStateManager: ViewStateManager = ViewStateManagerImpl
    private val mainScope = MainScope()

    protected open fun onPreCreate(): Boolean {
        return true
    }

    protected open fun onPostCreate() {

    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!onPreCreate()) return

        createView(savedInstanceState)
        onPostCreate()
    }

    protected fun runOnUI(executable: Executable): Job {
        return mainScope.launch {
            executable()
        }
    }


    protected abstract fun createView(savedInstanceState: Bundle?)

    override fun onBackPressed() {
        if (viewStateManager.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        viewStateManager.onScreenFinished()
    }

}

abstract class BaseViewModel : ViewModel(), LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry

    init {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    protected val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected fun runOnBackground(executable: Executable): Job {
        return backgroundScope.launch {
            executable()
        }

    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    protected suspend fun <T> runAndWaitOnBackground(executable: ExecutableForResult<T>): T {
        return withContext(backgroundScope.coroutineContext) {
            executable()
        }

    }

    override fun onCleared() {
        super.onCleared()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED

        backgroundScope.cancel()
    }
}

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application), LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry

    init {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    protected val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected fun runOnBackground(executable: Executable): Job {
        return backgroundScope.launch {
            executable()
        }

    }

    protected suspend fun <T> runAndWaitOnBackground(executable: ExecutableForResult<T>): T {
        return withContext(backgroundScope.coroutineContext) {
            executable()
        }

    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onCleared() {
        super.onCleared()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        backgroundScope.cancel()
    }
}

abstract class BaseFragment : Fragment() {
    protected val viewStateManager: ViewStateManager = ViewStateManagerImpl
    private val mainScope = MainScope()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onPostCreate(savedInstanceState)
    }

    protected fun showToast(yourString: String) {
        Toast.makeText(context, yourString, Toast.LENGTH_SHORT).show()
    }

    protected fun getBaseActivity(): BaseActivity? {
        if (activity != null) {
            return activity as BaseActivity
        }
        return null
    }

    protected open fun onPostCreate(savedInstanceState: Bundle?) {

    }

    protected fun runOnUI(executable: Executable): Job {
        return mainScope.launch {
            executable()
        }
    }


    final override fun onDestroy() {
        super.onDestroy()
        onPostDestroy()
        mainScope.cancel()
        viewStateManager.onScreenFinished()
    }

    protected open fun onPostDestroy() {

    }

    open fun onReceivedAction(fragmentMeta: FragmentMeta) {
    }

    open fun sendFragmentAction(fragmentName: String, action: String, data: Any? = null) {
        if (activity is IMyStudioActivity) {
            (activity as IMyStudioActivity).sendAction(FragmentMeta(fragmentName, action, data))
        }
    }
}

abstract class BaseDialog : DialogFragment() {
    protected abstract fun getLayoutResId(): Int
    private val mainScope = MainScope()
    override fun show(manager: FragmentManager, tag: String?) {
        validateDialogTag(tag)
        super.show(manager, tag)
    }

    protected fun runOnUI(executable: Executable): Job {
        return mainScope.launch {
            executable()
        }
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        TODO("not implemented")
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        validateDialogTag(tag)
        super.showNow(manager, tag)
    }

    private fun validateDialogTag(tag: String?) {
        tag?.let {
            if (it.isEmpty()) {
                throw IllegalArgumentException("tag is not empty")
            }
        } ?: throw IllegalArgumentException("tag is not null")
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(getLayoutResId(), container, false)
        initViews(view, savedInstanceState)
        bindEvents()
        return view
    }

    open protected fun initViews(view: View, savedInstanceState: Bundle?) {

    }

    open protected fun bindEvents() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let { window ->
            arguments?.let {
                val style = it.getInt(DIALOG_STYLE_KEY, -1)
                if (style != -1) {
                    window.attributes.windowAnimations = style
                }
            }
        }
    }

    fun setStyle(style: Int) {
        arguments?.putInt(DIALOG_STYLE_KEY, style) ?: let {
            val bundle = Bundle()
            bundle.putInt(DIALOG_STYLE_KEY, style)
        }
    }
}


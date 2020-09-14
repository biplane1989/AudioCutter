package com.example.audiocutter.base

import android.app.Application
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import androidx.lifecycle.*
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateManager
import com.example.a0025antivirusapplockclean.base.viewstate.ViewStateManagerImpl
import com.example.audiocutter.base.channel.FragmentChannel
import com.example.audiocutter.base.channel.FragmentMeta
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
        super.onBackPressed()
        viewStateManager.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
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

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application),
    LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry

    init {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    private val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected fun runOnBackgroundThread(executable: Executable): Job {
        return backgroundScope.launch {
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
    protected lateinit var baseActivity: BaseActivity;
    private val mainScope = MainScope()
    private val fragmentChannelObserver = Observer<FragmentMeta> {
        if (it.clsName.equals(this::class.java.name)) {
            onReceivedAction(it)
        }
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentChannel.getFragmentMeta().observe(this, fragmentChannelObserver)
        onPostCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        baseActivity = activity as BaseActivity
    }

    protected open fun onPostCreate(savedInstanceState: Bundle?) {

    }

    protected fun runOnUI(executable: Executable): Job {
        return mainScope.launch {
            executable()
        }
    }

    protected fun sendFragmentAction(fragmentName:String, action: String, data: Any? = null) {
        FragmentChannel.sendAction(FragmentMeta(fragmentName, action, data))
    }

    final override fun onDestroy() {
        super.onDestroy()
        onPostDestroy()
        FragmentChannel.getFragmentMeta().removeObserver(fragmentChannelObserver)
        mainScope.cancel()
    }

    protected open fun onPostDestroy() {

    }

    protected open fun onReceivedAction(fragmentMeta: FragmentMeta) {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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


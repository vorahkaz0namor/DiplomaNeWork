package ru.sign.conditional.diplomanework.util

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import ru.sign.conditional.diplomanework.R
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null
    private val lifecycleObserver = BindingLifeCycleObserver()

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return  it }

        val lifeCycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifeCycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
            throw IllegalStateException(
                fragment.getString(R.string.fragment_is_dead)
            )

        thisRef.viewLifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        return viewBindingFactory(thisRef.requireView())
                   .also { binding = it }
    }

    private inner class BindingLifeCycleObserver : DefaultLifecycleObserver {
        private val mainHandler = Handler(Looper.getMainLooper())

        @MainThread
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            mainHandler.post { binding = null }
        }
    }
}
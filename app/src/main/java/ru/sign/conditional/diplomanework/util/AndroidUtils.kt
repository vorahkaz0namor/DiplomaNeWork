package ru.sign.conditional.diplomanework.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.sign.conditional.diplomanework.R
import java.io.File

object AndroidUtils {
    val defaultDispatcher = Dispatchers.Default
    val Fragment.viewScope
        get() = viewLifecycleOwner.lifecycleScope

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /** Adjusts resize of the fragment when it overlaps by the other objects */
    fun Activity.layoutSizeAdjust(rootView: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        } else {
            window?.setDecorFitsSystemWindows(false)
            rootView.onApplyWindowInsets(WindowInsets.CONSUMED)
        }
    }

    fun Fragment.viewScopeWithRepeat(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }

    /** Creates launcher to load the image */
    fun Fragment.createImageLauncher(
        view: View, action: ImageListener
    ): ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == ImagePicker.RESULT_ERROR) {
                Snackbar.make(
                    view,
                    getString(R.string.error_loading),
                    Snackbar.LENGTH_LONG
                )
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show()
            } else {
                val uri = it.data?.data
                    ?: return@registerForActivityResult
                action.getImage(uri, uri.toFile())
            }
        }

    fun interface ImageListener {
        fun getImage(
            uri: Uri,
            file: File
        )
    }

    fun MenuProvider?.validationToCreateMenu(
        controller: NavController,
        @IdRes vararg destIds: Int
    ): Boolean =
        if (this == null) {
            destIds.map {
                controller.currentDestination?.id == it
            }
                .contains(true)
        } else
            false
}
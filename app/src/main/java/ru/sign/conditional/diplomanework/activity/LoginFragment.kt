package ru.sign.conditional.diplomanework.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentLoginBinding
import ru.sign.conditional.diplomanework.util.AndroidUtils
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel

class LoginFragment : DialogFragment(R.layout.fragment_login) {
    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var avatarLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        avatarLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == ImagePicker.RESULT_ERROR)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.image_error),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show()
                else
                    (result.data?.data
                        ?: return@registerForActivityResult).also {
                            authViewModel.addAvatar(it, it.toFile())
                        }
            }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        else {
            dialog?.window?.setDecorFitsSystemWindows(false)
            binding.root.onApplyWindowInsets(WindowInsets.CONSUMED)
        }
    }

    private fun subscribe() {
        binding.apply {
            setInvisibleErrorWrongLoginPassword(loginField.editText, passwordField.editText)
            if (authViewModel.authState.value?.regShowing == true)
                setVisibilityErrorPasswordsDontMatch(passwordField.editText, confirmPasswordField.editText)
        }
        authViewModel.apply {
            authState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    authViewGroup.isVisible = state.authShowing
                    regViewGroup.isVisible = state.regShowing
                    commonViewGroup.isVisible = state.authShowing || state.regShowing
                }
            }
            media.observe(viewLifecycleOwner) { avatar ->
                binding.apply {
                    addAvatarImage.isVisible =
                        avatar == null &&
                        authState.value?.regShowing == true
                    avatarPreviewGroup.isVisible = avatar != null
                    avatarPreview.setImageURI(avatar?.uri)
                }
            }
            authEvent.observe(viewLifecycleOwner) { code ->
                if (code == HTTP_OK) {
                    if (authState.value?.regShowing == true)
                        Toast.makeText(
                            context,
                            getString(R.string.successful_reg_in),
                            Toast.LENGTH_LONG
                        ).show()
                    customNavigateUp()
                } else {
                    val condition = (authState.value?.authShowing == true &&
                                     code == HTTP_BAD_REQUEST || code == HTTP_NOT_FOUND)
                    binding.wrongLoginPassword.isVisible = condition
                    if (!condition)
                        Snackbar.make(
                            binding.root,
                            overview(code),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .setAction(android.R.string.ok) {
                                saveAuthError(code)
                                customNavigateUp()
                            }
                            .show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            addAvatarImage.setOnClickListener {
                ImagePicker.with(this@LoginFragment)
                    .galleryOnly()
                    .crop()
                    .createIntent { intent ->
                        avatarLauncher.launch(intent)
                    }
            }
            clearAvatar.setOnClickListener { authViewModel.clearAvatar() }
            loginButton.setOnClickListener {
                AndroidUtils.hideKeyboard(root)
                if (textValidation())
                    authViewModel.login(
                        login = loginField.editText?.text.toString(),
                        password = passwordField.editText?.text.toString()
                    )
            }
            regButton.setOnClickListener {
                AndroidUtils.hideKeyboard(root)
                avatarPreviewGroup.isVisible = false
                if (textValidation() &&
                    passwordField.editText?.text
                        .contentEquals(confirmPasswordField.editText?.text))
                    authViewModel.register(
                        login = loginField.editText?.text.toString(),
                        password = passwordField.editText?.text.toString(),
                        name =
                        if (!nameField.editText?.text.isNullOrBlank())
                            nameField.editText?.text.toString()
                        else
                            "User"
                    )
            }
            cancelButton.setOnClickListener { customNavigateUp() }
        }
    }

    private fun textValidation() = (
        !binding.loginField.editText?.text.isNullOrBlank() &&
        !binding.passwordField.editText?.text.isNullOrBlank()
    )

    private fun setInvisibleErrorWrongLoginPassword(vararg text: EditText?) {
        text.map {
            it?.addTextChangedListener {
                binding.wrongLoginPassword.isVisible = false
            }
        }
    }

    private fun setVisibilityErrorPasswordsDontMatch(vararg text: EditText?) {
        text.map {
            it?.addTextChangedListener { field ->
                binding.passwordsDontMatch.isVisible =
                    (!field.contentEquals(binding.passwordField.editText?.text) ||
                     !field.contentEquals(binding.confirmPasswordField.editText?.text))
            }
        }
    }

    private fun customNavigateUp() {
        authViewModel.clearAvatar()
        findNavController().navigateUp()
    }

    override fun onStop() {
        AndroidUtils.hideKeyboard(binding.root)
        super.onStop()
    }

    override fun onDismiss(dialog: DialogInterface) {
        authViewModel.clearAvatar()
        super.onDismiss(dialog)
    }
}
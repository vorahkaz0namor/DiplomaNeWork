package ru.sign.conditional.diplomanework.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel

class AuthDialogFragment : DialogFragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .let {
                if (authViewModel.authorized) {
                    it.setTitle(getString(R.string.sure_to_logout))
                    it.setPositiveButton(getString(R.string.logout_item)) { _, _ ->
                        authViewModel.logout()
                        findNavController().navigate(R.id.feedFragment)
                    }
                } else {
                    it.setTitle(getString(R.string.must_to_login))
                    it.setMessage(getString(R.string.wish_to_login))
                    it.setPositiveButton(getString(R.string.log_in)) { _, _ ->
                        authViewModel.authShowing()
                        findNavController().navigate(R.id.loginFragment)
                        dismiss()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

    companion object {
        const val AUTH_TAG = "AuthenticationDialog"
    }
}
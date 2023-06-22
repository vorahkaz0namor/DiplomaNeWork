package ru.sign.conditional.diplomanework.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appNavController: NavController
    private var currentMenuProvider: MenuProvider? = null
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setAuthMenu()
        subscribe()
        setupListeners()
    }

    private fun init() {
        appNavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(appNavController.graph)
        setupActionBarWithNavController(appNavController, appBarConfiguration)
    }

    private fun setAuthMenu() {
        if (currentMenuProvider == null &&
                appNavController
                    .currentDestination
                    ?.id
                ==
                appNavController
                    .graph
                    .startDestinationId) {
            currentMenuProvider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.login -> {
                            authViewModel.authShowing()
                            appNavController.navigate(R.id.loginFragment)
                            true
                        }
                        R.id.register -> {
                            authViewModel.regShowing()
                            appNavController.navigate(R.id.loginFragment)
                            true
                        }
                        R.id.logout -> {
                            AuthDialogFragment().show(
                                supportFragmentManager,
                                AuthDialogFragment.AUTH_TAG
                            )
                            true
                        }
                        else -> false
                    }
            }
            addMenuProvider(currentMenuProvider as MenuProvider, this)
        }
    }

    private fun subscribe() {
        authViewModel.data.observe(this) {
            setAuthMenu()
        }
    }

    private fun setupListeners() {
        appNavController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment -> if (currentMenuProvider == null) setAuthMenu()
                R.id.loginFragment,
                R.id.editPostFragment,
                R.id.attachmentFragment ->
                    currentMenuProvider?.let {
                        removeMenuProvider(it)
                        currentMenuProvider = null
                    }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return appNavController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }
}

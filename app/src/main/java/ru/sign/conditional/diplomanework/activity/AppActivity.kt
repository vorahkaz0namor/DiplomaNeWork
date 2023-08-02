package ru.sign.conditional.diplomanework.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.ActionProvider
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.util.AndroidUtils.validationToCreateMenu
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appNavController: NavController
    private var currentMenuProvider: MenuProvider? = null
    private lateinit var postsActionProvider: ActionProvider
    private lateinit var eventsActionProvider: ActionProvider
    private lateinit var jobsActionProvider: ActionProvider
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setAppMenu()
        subscribe()
        setupListeners()
    }

    private fun init() {
        setSupportActionBar(findViewById(R.id.custom_toolbar))
        appNavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(appNavController.graph)
        setupActionBarWithNavController(appNavController, appBarConfiguration)
        postsActionProvider =
            createActionProvider(
                layoutId = R.layout.posts_action_provider,
                viewId = R.id.show_posts,
                destId = R.id.feedPostFragment
            ) { appNavController.navigate(R.id.feedPostFragment) }
        eventsActionProvider =
            createActionProvider(
                layoutId = R.layout.events_action_provider,
                viewId = R.id.show_events,
                destId = R.id.feedEventFragment
            ) { appNavController.navigate(R.id.feedEventFragment) }
        jobsActionProvider =
            createActionProvider(
                layoutId = R.layout.jobs_action_provider,
                viewId = R.id.show_jobs,
                destId = R.id.feedJobFragment
            ) { appNavController.navigate(R.id.feedJobFragment) }
    }

    private fun setAppMenu() {
        if (
            currentMenuProvider.validationToCreateMenu(
                controller = appNavController,
                destIds = intArrayOf(
                    R.id.feedPostFragment,
                    R.id.feedEventFragment,
                    R.id.feedJobFragment
                )
            )
        ) {
            currentMenuProvider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.app_menu, menu)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    menu.findItem(R.id.posts_list).actionView =
                        postsActionProvider.onCreateActionView()
                    menu.findItem(R.id.events_list).actionView =
                        eventsActionProvider.onCreateActionView()
                    if (authViewModel.authorized) {
                        menu.findItem(R.id.jobs_list).actionView =
                            jobsActionProvider.onCreateActionView()
                    }
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
            addMenuProvider(
                /* provider = */ currentMenuProvider as MenuProvider,
                /* owner = */ this)
        }
    }

    private fun subscribe() {
        authViewModel.data.observe(this) {
            setAppMenu()
        }
    }

    private fun setupListeners() {
        appNavController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedPostFragment,
                R.id.feedEventFragment,
                R.id.feedJobFragment -> {
                    if (currentMenuProvider == null) setAppMenu()
                }
                R.id.loginFragment,
                R.id.editPostFragment,
                R.id.editEventFragment,
                R.id.singleEventFragment,
                R.id.attachmentFragment,
                R.id.editJobFragment -> {
                    currentMenuProvider?.let {
                        removeMenuProvider(it)
                        currentMenuProvider = null
                    }
                }
            }
        }
    }

    private fun createActionProvider(
        @LayoutRes layoutId: Int,
        @IdRes viewId: Int,
        @IdRes destId: Int,
        action: () -> Unit
    ): ActionProvider = object : ActionProvider(this@AppActivity) {
        override fun onCreateActionView(): View =
            LayoutInflater.from(this@AppActivity)
                .inflate(
                    /* resource = */ layoutId,
                    /* root = */ null
                )
                .also {
                    (it as MaterialButton)
                        .findViewById<MaterialButton>(viewId).apply {
                            setOnClickListener { action() }
                            appNavController
                                .addOnDestinationChangedListener { _, dest, _ ->
                                    isChecked = dest.id == destId
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
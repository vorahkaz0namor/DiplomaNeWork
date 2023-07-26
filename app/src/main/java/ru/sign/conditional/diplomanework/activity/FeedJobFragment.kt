package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.retry
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.adapter.JobAdapter
import ru.sign.conditional.diplomanework.adapter.OnJobInteractionListenerImpl
import ru.sign.conditional.diplomanework.databinding.FragmentFeedJobBinding
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScopeWithRepeat
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.JobViewModel

class FeedJobFragment : Fragment(R.layout.fragment_feed_job) {
    private val binding by viewBinding(FragmentFeedJobBinding::bind)
    private val jobViewModel: JobViewModel by activityViewModels()
    private lateinit var jobAdapter: JobAdapter
    private lateinit var navController: NavController
    private var snackbar: Snackbar? = null

    override fun onDestroyView() {
        snackbarDismiss()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setUpListeners()
    }

    private fun initViews() {
        jobAdapter = JobAdapter(
            OnJobInteractionListenerImpl(jobViewModel)
        )
        binding.jobs.apply {
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = jobAdapter
        }
        navController = findNavController()
    }

    private fun subscribe() {
        jobViewModel.apply {
            // Отображение списка работ
            viewScopeWithRepeat {
                data.collectLatest {
                    snackbarDismiss()
                    jobAdapter.submitList(it)
                    binding.emptyView.emptyJobsList.isVisible = it.isEmpty()
                }
            }
            // Состояние списка работ
            dataState.observe(viewLifecycleOwner) { state ->
                snackbarDismiss()
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    jobsGroupView.isVisible = state.showing
                    errorView.errorMessage.isVisible = state.error
                }
            }
            // Редактирование работы
            edited.observe(viewLifecycleOwner) { job ->
                if (job.id != 0)
                    navController.navigate(
                        R.id.action_feedJobFragment_to_editJobFragment
                    )
            }
            // Обработка ошибок при загрузке списка работ
            jobEvent.observe(viewLifecycleOwner) { code ->
                if (code != HTTP_OK) {
                    snackbar = Snackbar.make(
                        binding.root,
                        overview(code),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .setAction(R.string.retry_loading) {
                            snackbarDismiss()
                            data.retry()
                        }
                    snackbar?.show()
                }
            }
        }
    }

    private fun setUpListeners() {
        // Добавление работы
        requireActivity().addMenuProvider(
            /* provider = */ object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.add_job_item_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.add_job -> {
                            navController.navigate(
                                R.id.action_feedJobFragment_to_editJobFragment
                            )
                            true
                        }
                        else -> false
                    }
            },
            /* owner = */ viewLifecycleOwner
        )
    }

    private fun snackbarDismiss() {
        if (snackbar != null && snackbar?.isShown == true) {
            snackbar?.dismiss()
            snackbar = null
        }
    }
}
package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.adapter.FeedItemAdapter
import ru.sign.conditional.diplomanework.adapter.FeedItemLoadingStateAdapter
import ru.sign.conditional.diplomanework.adapter.OnEventInteractionListenerImpl
import ru.sign.conditional.diplomanework.databinding.FragmentFeedEventBinding
import ru.sign.conditional.diplomanework.model.RemotePresentationState.PRESENTED
import ru.sign.conditional.diplomanework.model.UiAction
import ru.sign.conditional.diplomanework.model.asRemotePresentationState
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScope
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScopeWithRepeat
import ru.sign.conditional.diplomanework.util.NeWorkHelper
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.EventViewModel

class FeedEventFragment : Fragment(R.layout.fragment_feed_event) {
    private val binding by viewBinding(FragmentFeedEventBinding::bind)
    private val authViewModel: AuthViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val attachmentViewModel: AttachmentViewModel by activityViewModels()
    private lateinit var eventAdapter: FeedItemAdapter
    private lateinit var loadStateHeader: FeedItemLoadingStateAdapter
    private lateinit var loadStateFooter: FeedItemLoadingStateAdapter
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
        setupListeners()
    }

    private fun initViews() {
        eventAdapter = FeedItemAdapter(
            OnEventInteractionListenerImpl(
                eventViewModel = eventViewModel,
                attachmentViewModel = attachmentViewModel,
                authViewModel = authViewModel
            )
        )
        loadStateHeader = FeedItemLoadingStateAdapter { eventAdapter.retry() }
        loadStateFooter = FeedItemLoadingStateAdapter { eventAdapter.retry() }
        binding.recyclerView.events.apply {
            adapter =
                eventAdapter.withLoadStateHeaderAndFooter(
                    header = loadStateHeader,
                    footer = loadStateFooter
                )
        }
        // Учет очередного запуска FeedEventFragment'а
        eventViewModel.appealTo()
        navController = findNavController()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribe() {
        eventViewModel.apply {
            // Отображение списка событий
            viewScopeWithRepeat {
                dataFlow.collectLatest {
                    snackbarDismiss()
                    eventAdapter.submitData(it)
                }
            }
            // Прокрутка обновленного списка событий
            viewScopeWithRepeat {
                val presented = eventAdapter.loadStateFlow
                    .asRemotePresentationState()
                    .mapLatest { state ->
                        snackbarDismiss()
                        val presented = state == PRESENTED
                        val isShownAdapterFirstItem =
                            binding.recyclerView.events
                                .findViewHolderForAdapterPosition(0)
                                ?.itemView
                                ?.isShown
                        if (presented &&
                            eventViewModel.appealTo == 1L &&
                            isShownAdapterFirstItem != true) {
                            binding.recyclerView.events.smoothScrollToPosition(0)
                            stateChanger(UiAction.Scroll(currentId = totalState.value.id))
                        } else {
                            binding.recyclerView.events.stopScroll()
                        }
                        presented
                    }
                val hasNotScrolledToCurrentId =
                    totalState.mapLatest {
                        it.hasNotScrolledToCurrentId
                    }
                        .distinctUntilChanged()
                val shouldScrollToTop =
                    combine(presented, hasNotScrolledToCurrentId) { conditionOne, conditionTwo ->
                        conditionOne.and(conditionTwo)
                    }
                        .distinctUntilChanged()
                shouldScrollToTop.collectLatest {
                    if (it) {
                        binding.recyclerView.events.smoothScrollToPosition(0)
                        val currentId = totalState.value.id
                        stateChanger(UiAction.Scroll(currentId = currentId))
                    }
                }
            }
            // Состояние загрузки событий
            viewScope.launch {
                eventAdapter.loadStateFlow.collectLatest { loadState ->
                    snackbarDismiss()
                    loadStateHeader.loadState =
                        loadState.mediator?.refresh
                            .takeIf {
                                it is LoadState.Loading ||
                                it is LoadState.Error
                            }
                        ?: loadState.mediator?.prepend
                            .takeIf {
                                it is LoadState.Loading ||
                                it is LoadState.Error
                            }
                        ?: loadState.source.refresh
                    loadStateFooter.loadState =
                        loadState.mediator?.append
                            .takeIf {
                                it is LoadState.Loading ||
                                        it is LoadState.Error
                            }
                        ?: loadState.source.append
                    val errorState = loadState.refresh as? LoadState.Error
                        ?: loadState.prepend as? LoadState.Error
                        ?: loadState.append as? LoadState.Error
                    binding.apply {
                        recyclerView.eventsRefresh.isRefreshing =
                            loadState.mediator?.refresh is LoadState.Loading
                        eventsGroupView.isVisible = errorState !is LoadState.Error
                        if (errorState is LoadState.Error) {
                            errorView.errorMessage.isVisible = true
                            recyclerView.eventsRefresh.isRefreshing = false
                        } else
                            errorView.errorMessage.isVisible = false
                    }
                    errorState?.let {
                        snackbar = Snackbar.make(
                            binding.root,
                            it.error.message ?: NeWorkHelper.overview(NeWorkHelper.HTTP_UNKNOWN_ERROR),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setTextMaxLines(3)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .setAction(R.string.retry_loading) {
                                snackbarDismiss()
                                eventAdapter.refresh()
                            }
                        snackbar?.show()
                    }
                }
            }
            // Редактирование события
            edited.observe(viewLifecycleOwner) { event ->
                if (event.id != 0)
                    navController.navigate(
                        R.id.action_global_editEventFragment
                    )
            }
            // Переход на карточку события
            eventIdToView.observe(viewLifecycleOwner) { id ->
                if (id != 0)
                    navController.navigate(
                        R.id.action_feedEventFragment_to_singleEventFragment
                    )
            }
        }
        // Просмотр вложения события
        attachmentViewModel.viewAttachment.observe(viewLifecycleOwner) { item ->
            if (item.id != 0)
                navController.navigate(
                    R.id.action_global_attachmentFragment
                )
        }
        authViewModel.apply {
            // Изменение состяния аутентификации
            viewScope.launch {
                data.observe(viewLifecycleOwner) {
                    snackbarDismiss()
                    eventAdapter.refresh()
                }
            }
            // Проверка авторизации
            checkAuthorized.observe(viewLifecycleOwner) {
                if (it) {
                    if (!authorized)
                        AuthDialogFragment().show(
                            childFragmentManager,
                            AuthDialogFragment.AUTH_TAG
                        )
                }
            }
            // Проброс ошибки аутентификации
            viewScope.launch {
                authError.observe(viewLifecycleOwner) { code ->
                    if (code != HTTP_OK &&
                        (code != HTTP_BAD_REQUEST ||
                                code != HTTP_NOT_FOUND)
                    ) {
                        clearAuthError()
                        eventAdapter.refresh()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.recyclerView.apply {
            // Создание нового события
            createNewEvent.setOnClickListener {
                if (!authViewModel.authorized)
                    AuthDialogFragment().show(
                        childFragmentManager,
                        AuthDialogFragment.AUTH_TAG
                    )
                else {
                    eventViewModel.getDraftCopy()
                    navController.navigate(
                        R.id.action_global_editEventFragment
                    )
                }
            }
            // Обновление списка событий после свайпа по нему
            viewScope.launch {
                eventsRefresh.setOnRefreshListener {
                    eventAdapter.refresh()
                }
            }
        }
    }

    private fun snackbarDismiss() {
        if (snackbar != null && snackbar?.isShown == true) {
            snackbar?.dismiss()
            snackbar = null
        }
    }
}
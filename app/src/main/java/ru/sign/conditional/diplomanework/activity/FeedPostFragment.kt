package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
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
import ru.sign.conditional.diplomanework.adapter.OnPostInteractionListenerImpl
import ru.sign.conditional.diplomanework.databinding.FragmentFeedPostBinding
import ru.sign.conditional.diplomanework.model.RemotePresentationState.*
import ru.sign.conditional.diplomanework.model.UiAction
import ru.sign.conditional.diplomanework.model.asRemotePresentationState
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScope
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScopeWithRepeat
import ru.sign.conditional.diplomanework.util.NeWorkHelper.HTTP_UNKNOWN_ERROR
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.NeWorkHelper.setRoundingRadius
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.PostViewModel

class FeedPostFragment : Fragment(R.layout.fragment_feed_post) {
    private val binding by viewBinding(FragmentFeedPostBinding::bind)
    private val authViewModel: AuthViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val attachmentViewModel: AttachmentViewModel by activityViewModels()
    private lateinit var postAdapter: FeedItemAdapter
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
        postAdapter = FeedItemAdapter(
            OnPostInteractionListenerImpl(
                postViewModel = postViewModel,
                attachmentViewModel = attachmentViewModel,
                authViewModel = authViewModel
            )
        )
        loadStateHeader = FeedItemLoadingStateAdapter { postAdapter.retry() }
        loadStateFooter = FeedItemLoadingStateAdapter { postAdapter.retry() }
        binding.recyclerView.posts.apply {
            adapter =
                postAdapter.withLoadStateHeaderAndFooter(
                    header = loadStateHeader,
                    footer = loadStateFooter
                )
        }
        viewScope.launch {
            // Учет очередного запуска FeedFragment'а
            postViewModel.appealTo()
            // Задание радиуса скругления для метода
            // NeWorkHelper.loadImage(...)
            if (postViewModel.appealTo == 1L)
                setRoundingRadius(
                    requireContext()
                        .resources
                        .getDimension(R.dimen.card_padding))
        }
        navController = findNavController()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribe() {
        postViewModel.apply {
            // Отображение списка постов
            viewScopeWithRepeat {
                dataFlow.collectLatest {
                    snackbarDismiss()
                    postAdapter.submitData(it)
                }
            }
            // Прокрутка обновленного списка постов
            viewScopeWithRepeat {
                val presented = postAdapter.loadStateFlow
                    .asRemotePresentationState()
                    .mapLatest { state ->
                        snackbarDismiss()
                        val presented = state == PRESENTED
                        val isShownAdapterFirstItem =
                            binding.recyclerView.posts
                                .findViewHolderForAdapterPosition(0)
                                ?.itemView
                                ?.isShown
                        if (presented &&
                            postViewModel.appealTo == 1L &&
                            isShownAdapterFirstItem != true) {
                            binding.recyclerView.posts.smoothScrollToPosition(0)
                            stateChanger(UiAction.Scroll(currentId = totalState.value.id))
                        } else {
                            binding.recyclerView.posts.stopScroll()
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
                        binding.recyclerView.posts.smoothScrollToPosition(0)
                        val currentId = totalState.value.id
                        stateChanger(UiAction.Scroll(currentId = currentId))
                    }
                }
            }
            // Состояние загрузки постов
            viewScope.launch {
                postAdapter.loadStateFlow.collectLatest { loadState ->
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
                        recyclerView.postsRefresh.isRefreshing =
                            loadState.mediator?.refresh is LoadState.Loading
                        postsGroupView.isVisible = errorState !is LoadState.Error
                        if (errorState is LoadState.Error) {
                            errorView.errorMessage.isVisible = true
                            recyclerView.postsRefresh.isRefreshing = false
                        } else
                            errorView.errorMessage.isVisible = false
                    }
                    errorState?.let {
                        snackbar = Snackbar.make(
                            binding.root,
                            it.error.message ?: overview(HTTP_UNKNOWN_ERROR),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setTextMaxLines(3)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .setAction(R.string.retry_loading) {
                                snackbarDismiss()
                                postAdapter.refresh()
                            }
                        snackbar?.show()
                    }
                }
            }
            // Редактирование поста
            edited.observe(viewLifecycleOwner) { post ->
                if (post.id != 0)
                    navController.navigate(
                        R.id.action_feedFragment_to_editPostFragment
                    )
            }
        }
        // Просмотр вложения поста
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
                    postAdapter.refresh()
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
                        postAdapter.refresh()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.recyclerView.apply {
            // Создание нового поста
            addNewPost.setOnClickListener {
                if (!authViewModel.authorized)
                    AuthDialogFragment().show(
                        childFragmentManager,
                        AuthDialogFragment.AUTH_TAG
                    )
                else {
                    postViewModel.getDraftCopy()
                    navController.navigate(
                        R.id.action_feedFragment_to_editPostFragment
                    )
                }
            }
            // Обновление списка постов после свайпа по нему
            viewScope.launch {
                postsRefresh.setOnRefreshListener {
                    postAdapter.refresh()
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
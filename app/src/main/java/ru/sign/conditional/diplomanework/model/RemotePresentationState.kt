package ru.sign.conditional.diplomanework.model

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.scan
import ru.sign.conditional.diplomanework.model.RemotePresentationState.*

enum class RemotePresentationState {
    INITIAL,
    REMOTE_LOADING,
    SOURCE_LOADING,
    PRESENTED
}

fun Flow<CombinedLoadStates>.asRemotePresentationState() : Flow<RemotePresentationState> =
    scan(INITIAL) { state, loadState ->
        when (state) {
            INITIAL, PRESENTED ->
                when {
                    loadState.mediator?.refresh is LoadState.Loading ||
                            loadState.mediator?.prepend is LoadState.Loading -> REMOTE_LOADING
                    loadState.source.refresh is LoadState.Loading -> SOURCE_LOADING
                    loadState.mediator?.append is LoadState.Loading -> INITIAL
                    else -> state
                }
            REMOTE_LOADING ->
                when (loadState.source.refresh) {
                    is LoadState.Loading -> SOURCE_LOADING
                    else -> state
                }
            SOURCE_LOADING ->
                when (loadState.refresh) {
                    is LoadState.Loading -> REMOTE_LOADING
                    is LoadState.NotLoading -> PRESENTED
                    else -> state
                }
        }
    }
        .distinctUntilChanged()
package ru.sign.conditional.diplomanework.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_CONTINUE
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.dto.DraftCopy
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.dto.UserPreview
import ru.sign.conditional.diplomanework.model.MediaModel
import ru.sign.conditional.diplomanework.model.UiAction
import ru.sign.conditional.diplomanework.model.UiState
import ru.sign.conditional.diplomanework.repository.PostRepository
import ru.sign.conditional.diplomanework.repository.UserRepository
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import ru.sign.conditional.diplomanework.util.NeWorkHelper.customLog
import ru.sign.conditional.diplomanework.util.NeWorkHelper.exceptionCheck
import ru.sign.conditional.diplomanework.util.SingleLiveEvent
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // Счетчик количества запусков FeedFragment'а
    private var _appealTo = 0L
    val appealTo: Long
        get() = _appealTo
    private val emptyPost = Post(
        id = 0,
        author = "",
        content = "",
        published = ""
    )
    private val cachedPagingDataFromRepo: Flow<PagingData<FeedItem>>
    val dataFlow: Flow<PagingData<FeedItem>>
        get() = cachedPagingDataFromRepo
    val stateChanger: (UiAction) -> Unit
    val totalState: StateFlow<UiState>
    private val _postEvent = SingleLiveEvent(HTTP_CONTINUE)
    val postEvent: LiveData<Int>
        get() = _postEvent
    private val _edited = MutableLiveData(emptyPost)
    val edited: LiveData<Post>
        get() = _edited
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media
    private var _draftCopy: DraftCopy? = null
    val draftCopy: DraftCopy?
        get() = _draftCopy

    init {
        val initialId = 0
        val actionStateFlow = MutableSharedFlow<UiAction>()
        stateChanger = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }
        val idsGot = actionStateFlow
            .filterIsInstance<UiAction.Get>()
            .distinctUntilChanged()
            .onStart {
                emit( UiAction.Get(id = initialId) )
            }
        val idsScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted
                    .WhileSubscribed(stopTimeoutMillis = 7_000),
                replay = 1
            )
            .onStart {
                emit( UiAction.Scroll(currentId = initialId) )
            }
        cachedPagingDataFromRepo = postRepository.data
            .mapLatest {
                val maxId = postRepository.getLatestPostId()
                Log.d("GOT MAX POST ID", maxId.toString())
                stateChanger(UiAction.Get(id = maxId))
                it
            }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
        totalState = combine(idsGot, idsScrolled) { flowOne, flowTwo ->
            flowOne to flowTwo
        }
            .mapLatest { (get, scroll) ->
                val uiState = UiState(
                    id = get.id,
                    lastIdScrolled = scroll.currentId
                )
                Log.d("UPDATE COMMON STATE", uiState.toString())
                uiState
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted
                    .WhileSubscribed(stopTimeoutMillis = 7_000),
                initialValue = UiState()
            )
        Log.d("INIT VIEW MODEL", "appealTo = $appealTo")
    }

    // READ functions

    fun appealTo() = (++_appealTo).also {
        Log.d("FEED START", "$it times")
    }

    fun getDraftCopy() {
        viewModelScope.launch {
            try {
                _draftCopy = postRepository.getDraftCopy()
            } catch (e: Exception) {
                customLog("GET DRAFT COPY", e)
            }
        }
    }

    // CREATE & UPDATE functions

    fun setEditPost(post: Post) { _edited.value = post }

    fun setImage(uri: Uri, file: File) {
        _media.value = MediaModel(uri, file)
    }

    fun savePost(text: CharSequence?, link: CharSequence?) {
        if (!text.isNullOrBlank()) {
            saveLink(link)
            save(text.toString())
        }
        else
            _postEvent.value = HTTP_OK
    }

    private fun save(newContent: String) {
        viewModelScope.launch {
            _postEvent.value =
                try {
                    edited.value?.let {
                        val userPreview =
                            if (it.id == 0)
                                userRepository.getOwnerPreview()
                            else
                                UserPreview(
                                    name = it.author,
                                    avatar = it.authorAvatar
                                )
                        val post = it.copy(
                            author = userPreview.name,
                            authorAvatar = userPreview.avatar,
                            content = newContent,
                            published = LocalDateTime.now().toString()
                        )
                        postRepository.savePost(post, media.value)
                    }
                    HTTP_OK
                } catch (e: Exception) {
                    exceptionCheck(e)
                }
        }
    }

    fun repeatSavePost(post: Post) {
        viewModelScope.launch {
            _postEvent.value =
                try {
                    postRepository.savePost(post, media.value)
                    HTTP_OK
                } catch (e: Exception) {
                    exceptionCheck(e)
                }
        }
    }

    fun likePostById(post: Post) {
        viewModelScope.launch {
            try {
                val ownerId = userRepository.getOwnerId()
                postRepository.likePostById(post, ownerId)
            } catch (e: Exception) {
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    fun saveDraftCopy(draftCopy: DraftCopy?) {
        viewModelScope.launch {
            _draftCopy = draftCopy
            if (validationDraftCopy())
                try {
                    postRepository.saveDraftCopy(
                        draftCopy ?: DraftCopy()
                    )
                } catch (e: Exception) {
                    customLog("SAVING DRAFT COPY", e)
                }
        }
    }

    private fun validationDraftCopy() =
        (draftCopy != null && edited.value?.content != draftCopy?.postContent)
                || edited.value?.id == 0

    fun addLink() { _edited.value = _edited.value?.copy(link = "") }

    private fun saveLink(link: CharSequence?) {
        _edited.value =
            if (!link.isNullOrBlank()) {
                _edited.value?.copy(
                    link = link.toString().trim()
                )
            } else {
                _edited.value?.copy(link = null)
            }
    }

    // DELETE functions

    fun clearEditPost() { _edited.value = emptyPost }

    fun clearLink() { _edited.value = _edited.value?.copy(link = null) }

    fun clearImage() {
        _edited.value = _edited.value?.copy(
            attachment = null
        )
        _media.value = null
    }

    fun removePostById(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.removePostById(post)
            } catch (e: Exception) {
                _postEvent.value = exceptionCheck(e)
            }
        }
    }
}
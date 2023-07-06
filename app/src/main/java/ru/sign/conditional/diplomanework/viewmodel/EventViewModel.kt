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
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.UserPreview
import ru.sign.conditional.diplomanework.model.MediaModel
import ru.sign.conditional.diplomanework.model.UiAction
import ru.sign.conditional.diplomanework.model.UiState
import ru.sign.conditional.diplomanework.repository.EventRepository
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
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // Счетчик количества запусков FeedEventFragment'а
    private var _appealTo = 0L
    val appealTo: Long
        get() = _appealTo
    private val emptyEvent = Event(
        id = 0,
        author = "",
        content = "",
        datetime = "",
        published = ""
    )
    private val cachedPagingDataFromRepo: Flow<PagingData<FeedItem>>
    val dataFlow: Flow<PagingData<FeedItem>>
        get() = cachedPagingDataFromRepo
    private var _singleEvent: Flow<Event?> = flowOf(null)
    val singleEvent: Flow<Event?>
        get() = _singleEvent
    val stateChanger: (UiAction) -> Unit
    val totalState: StateFlow<UiState>
    private val _eventOccurrence = SingleLiveEvent(HTTP_CONTINUE)
    val eventOccurrence: LiveData<Int>
        get() = _eventOccurrence
    private val _edited = MutableLiveData(emptyEvent)
    val edited: LiveData<Event>
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
        cachedPagingDataFromRepo = eventRepository.data
            .mapLatest {
                val maxId = eventRepository.getLatestEventId()
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
                uiState
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted
                    .WhileSubscribed(stopTimeoutMillis = 7_000),
                initialValue = UiState()
            )
    }

    // READ functions

    fun appealTo() = (++_appealTo)

    fun getEventById(id: Int) {
        viewModelScope.launch {
            try {
                _singleEvent = eventRepository.getEventById(id)
                    .mapLatest { it }
                    .flowOn(defaultDispatcher)
            } catch (e: Exception) {
                _eventOccurrence.value = exceptionCheck(e)
            }
        }
    }

    fun getDraftCopy() {
        viewModelScope.launch {
            try {
                _draftCopy = eventRepository.getDraftCopy()
            } catch (e: Exception) {
                customLog("GET DRAFT COPY", e)
            }
        }
    }

    // CREATE & UPDATE functions

    fun setEditEvent(event: Event) { _edited.value = event }

    fun setImage(uri: Uri, file: File) {
        _media.value = MediaModel(uri, file)
    }

    fun saveEvent(text: CharSequence?) {
        if (!text.isNullOrBlank())
            save(text.toString())
        else
            _eventOccurrence.value = HTTP_OK
    }

    private fun save(newContent: String) {
        viewModelScope.launch {
            _eventOccurrence.value =
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
                        val event = it.copy(
                            author = userPreview.name,
                            authorAvatar = userPreview.avatar,
                            content = newContent,
                            published = LocalDateTime.now().toString()
                        )
                        eventRepository.saveEvent(event, media.value)
                    }
                    HTTP_OK
                } catch (e: Exception) {
                    exceptionCheck(e)
                }
        }
    }

    fun repeatSaveEvent(event: Event) {
        viewModelScope.launch {
            _eventOccurrence.value =
                try {
                    eventRepository.saveEvent(event, media.value)
                    HTTP_OK
                } catch (e: Exception) {
                    exceptionCheck(e)
                }
        }
    }

    fun editParticipantsInEventById(event: Event) {
        viewModelScope.launch {
            try {
                val ownerId = userRepository.getOwnerId()
                eventRepository.editParticipantsInEventById(event, ownerId)
            } catch (e: Exception) {
                _eventOccurrence.value = exceptionCheck(e)
            }
        }
    }

    fun saveDraftCopy(draftCopy: DraftCopy?) {
        viewModelScope.launch {
            _draftCopy = draftCopy
            if (validationDraftCopy())
                try {
                    eventRepository.saveDraftCopy(
                        draftCopy ?: DraftCopy()
                    )
                } catch (e: Exception) {
                    customLog("SAVING DRAFT COPY", e)
                }
        }
    }

    private fun validationDraftCopy() =
        (draftCopy != null && edited.value?.content != draftCopy?.eventContent)
                || edited.value?.id == 0

    // DELETE functions

    fun clearEditEvent() { _edited.value = emptyEvent }

    fun clearImage() {
        _edited.value = _edited.value?.copy(
            attachment = null
        )
        _media.value = null
    }

    fun removeEventById(event: Event) {
        viewModelScope.launch {
            try {
                eventRepository.removeEventById(event)
            } catch (e: Exception) {
                _eventOccurrence.value = exceptionCheck(e)
            }
        }
    }
}
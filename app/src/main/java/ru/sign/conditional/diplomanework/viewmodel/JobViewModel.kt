package ru.sign.conditional.diplomanework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.model.FeedModelState
import ru.sign.conditional.diplomanework.repository.JobRepository
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import ru.sign.conditional.diplomanework.util.DatetimeKeeper
import ru.sign.conditional.diplomanework.util.NeWorkDatetime
import ru.sign.conditional.diplomanework.util.NeWorkHelper.datetimeWithOffset
import ru.sign.conditional.diplomanework.util.NeWorkHelper.exceptionCheck
import ru.sign.conditional.diplomanework.util.SingleLiveEvent
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {
    private val emptyJob = Job(
        id = 0,
        name = "",
        position = "",
        start = ""
    )
    private lateinit var _data: Flow<List<Job>>
    val data: Flow<List<Job>>
        get() = _data
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val _jobEvent = SingleLiveEvent(HTTP_OK)
    val jobEvent: LiveData<Int>
        get() = _jobEvent
    private val _datesIsntValid = SingleLiveEvent(true)
    val datesIsntValid: LiveData<Boolean>
        get() = _datesIsntValid
    private val _edited = MutableLiveData(emptyJob)
    val edited: LiveData<Job>
        get() = _edited
    private val _startForLayout = MutableLiveData<NeWorkDatetime?>(null)
    val startForLayout: LiveData<NeWorkDatetime?>
        get() = _startForLayout
    private val _finishForLayout = MutableLiveData<NeWorkDatetime?>(null)
    val finishForLayout: LiveData<NeWorkDatetime?>
        get() = _finishForLayout
    private val keeper = DatetimeKeeper()

    init {
        // READ functions

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                _data = jobRepository.getMyJobs()
                    .mapLatest { it }
                    .flowOn(defaultDispatcher)
                    .distinctUntilChanged()
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted
                            .WhileSubscribed(stopTimeoutMillis = 7_000),
                        initialValue = emptyList()
                    )
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _jobEvent.value = exceptionCheck(e)
            }
        }
    }

    // CREATE & UPDATE functions

    fun setEditJob(job: Job) {
        viewModelScope.launch {
            _edited.value = job
            _startForLayout.value = datetimeWithOffset(job.start)
            if (!job.finish.isNullOrBlank())
                _finishForLayout.value = datetimeWithOffset(job.finish)
        }
    }

    fun setStart(date: NeWorkDatetime) {
        _startForLayout.value = keeper.jobDateValidation(date)
    }

    fun setFinish(date: NeWorkDatetime) {
        _finishForLayout.value = keeper.jobDateValidation(date)
    }

    fun datesValidationBeforeSaveJob() {
        viewModelScope.launch {
            val now = Instant.now().atOffset(OffsetDateTime.now().offset)
            _datesIsntValid.value =
                (
                    startForLayout.value?.jobDatetime()?.let {
                        it.year == now.year && it.monthValue > now.monthValue
                    } ?: true
                ).or(
                    finishForLayout.value?.jobDatetime()?.let { finish ->
                        (finish.year == now.year && finish.monthValue > now.monthValue) ||
                        startForLayout.value?.jobDatetime()?.let { start ->
                            start > finish
                        } ?: true
                    } ?: false
                )
        }
    }

    fun saveJob(
        name: String,
        position: String,
        link: String?
    ) {
        viewModelScope.launch {
            _jobEvent.value =
                try {
                    edited.value?.let {
                        jobRepository.saveJob(
                            it.copy(
                                name = name,
                                position = position,
                                start = startForLayout.value?.jobDatetime().toString(),
                                finish = finishForLayout.value?.jobDatetime()?.toString(),
                                link = link
                            )
                        )
                    }
                    HTTP_OK
                } catch (e: Exception) {
                    exceptionCheck(e)
                }
        }
    }

    // DELETE functions

    fun clearEditJob() { _edited.value = emptyJob }

    fun clearDates() {
        _startForLayout.value = null
        _finishForLayout.value = null
    }

    fun removeJobById(job: Job) {
        viewModelScope.launch {
            try {
                jobRepository.removeJobById(job)
            } catch (e: Exception) {
                _jobEvent.value = exceptionCheck(e)
            }
        }
    }
}
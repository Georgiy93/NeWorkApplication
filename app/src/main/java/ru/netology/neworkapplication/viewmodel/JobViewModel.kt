package ru.netology.neworkapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.repository.job.JobRepository
import ru.netology.neworkapplication.util.ResourceProvider
import ru.netology.neworkapplication.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",


    )

@Suppress("unused")
@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    private val resourceProvider: ResourceProvider,

    ) : ViewModel() {


    private val _messageError = SingleLiveEvent<String>()
    val messageError: LiveData<String>
        get() = _messageError

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>>
        get() = _jobs
    private val edited = MutableLiveData(empty)

    val editedJob: LiveData<Job>
        get() = edited
    private val _jobCreated = SingleLiveEvent<Unit>()

    val jobCreated: LiveData<Unit>
        get() = _jobCreated


    init {
        loadJobs()

    }

    fun loadJobs() = viewModelScope.launch {
        try {


            val jobs = repository.getJobAll().toList()
            _jobs.value = jobs
        } catch (e: Exception) {


            _messageError.value = e.message
        }
    }


    fun edit(jobId: Long) {

        viewModelScope.launch {
            try {

                val jobs = repository.getJobAll()
                val job = jobs.find { it.id == jobId }
                if (job != null) {
                    edited.value = job
                } else {
                    _messageError.value = resourceProvider.getString(R.string.job_not_found)
                }
            } catch (e: Exception) {
                _messageError.value = e.message
            }
        }
    }

    fun changeName(name: String) {
        val text = name.trim()
        if (edited.value?.name == text) {
            return
        }
        edited.value = edited.value?.copy(name = text)
    }

    fun changePosition(position: String) {
        val text = position.trim()
        if (edited.value?.position == text) {
            return
        }
        edited.value = edited.value?.copy(position = text)
    }

    fun changeStart(start: String) {
        val text = start.trim()
        if (edited.value?.start == text) {
            return
        }
        edited.value = edited.value?.copy(start = text)
    }

    fun changeFinish(finish: String?) {

        val text = finish?.trim()
        if (edited.value?.finish == text) {
            return
        }
        edited.value = edited.value?.copy(finish = text)
    }

    fun changeLink(link: String?) {
        val text = link?.trim()
        if (edited.value?.link == text) {
            return
        }
        edited.value = edited.value?.copy(link = text)
    }


    fun removeJobById(id: Long) {
        viewModelScope.launch {
            try {

                repository.removeJobById(id)
                val jobs = repository.getJobAll().toList()
                _jobs.value = jobs
            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }
    }

    fun save() {
        edited.value?.let { job ->
            viewModelScope.launch {
                try {

                    repository.saveJob(job)
                    _jobCreated.value = Unit

                } catch (_: Exception) {
                }
            }
        }
        edited.value = empty
    }
}
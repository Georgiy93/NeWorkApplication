package ru.netology.neworkapplication.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.model.FeedJobModel
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.repository.PostRepository
import ru.netology.neworkapplication.repository.job.JobRepository
import ru.netology.neworkapplication.util.SingleLiveEvent

import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",


    )

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    @ApplicationContext
    private val context: Context

) : ViewModel() {


    private val _messageError = SingleLiveEvent<String>(context)
    val messageError: LiveData<String>
        get() = _messageError
    private val editedContent = MutableLiveData("")
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>>
        get() = _jobs
    private val edited = MutableLiveData(empty)
    val editedJob: LiveData<Job>
        get() = edited
    private val _jobCreated = SingleLiveEvent<Unit>(context)
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


    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {

                    repository.saveJob(it)
                    _jobCreated.value = Unit
                    val jobs = repository.getJobAll().toList()
                    _jobs.value = jobs
                } catch (e: Exception) {
                }
            }
        }
        edited.value = empty
    }


    fun edit(jobId: Long) {
        viewModelScope.launch {
            try {

                val job = repository.getJob(jobId)
                val jobs = repository.getJobAll().toList()
                _jobs.value =
                    jobs
                if (job != null) {
                    edited.value = job
                } else {
                    _messageError.value = context.getString(R.string.job_not_found)
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
}
package ru.netology.neworkapplication.viewmodel

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
import ru.netology.neworkapplication.util.TokenManager
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",


    )

@HiltViewModel
@ExperimentalCoroutinesApi
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    private val tokenManager: TokenManager,
    auth: AppAuth,
) : ViewModel() {


    private val _messageError = SingleLiveEvent<String>()
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
    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated


    init {
        loadJobs()

    }

    fun loadJobs() = viewModelScope.launch {
        try {


            val token = tokenManager.getToken()
            val jobs = repository.getJobAll(token).toList()
            _jobs.value = jobs
        } catch (e: Exception) {

            Log.e("JobViewModel", "Error: ", e)
            _messageError.value = e.message
        }
    }


    fun save() {
        edited.value?.let {
            Log.d("JobViewModel", "Saving job: $it")
            viewModelScope.launch {
                try {
                    val token = tokenManager.getToken()
                    repository.saveJob(it, token)
                    _jobCreated.value = Unit
                    val jobs = repository.getJobAll(token).toList()
                    _jobs.value = jobs
                } catch (e: Exception) {
                    Log.e("JobViewModel", "Error saving job", e)
                }
            }
        }
        edited.value = empty
    }



    fun edit(jobId: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val job = repository.getJob(jobId)
                val jobs = repository.getJobAll(token).toList()
                _jobs.value =
                    jobs
                if (job != null) {
                    edited.value = job
                } else {
                    _messageError.value = "Job not found"
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


    fun removeJobById(id: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                repository.removeJobById(id, token)
                val jobs = repository.getJobAll(token).toList()
                _jobs.value = jobs
            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }
    }
}
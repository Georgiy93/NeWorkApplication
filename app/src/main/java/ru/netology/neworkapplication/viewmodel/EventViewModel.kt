package ru.netology.neworkapplication.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.FeedItemEvent
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.dto.UserPreview
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.model.MediaModel
import ru.netology.neworkapplication.repository.events.EventRepository
import ru.netology.neworkapplication.repository.job.JobRepository
import ru.netology.neworkapplication.util.SingleLiveEvent
import ru.netology.neworkapplication.util.TokenManager
import java.io.File
import javax.inject.Inject

private val empty = Event(
    id = 0,
    content = "",
    authorId = 0,
    author = "",
    authorAvatar = "",
    authorJob = "",
    likedByMe = false,
    published = "",
    datetime = "",
    link = null,
    type = "OFFLINE",
    participatedByMe = true


)


@HiltViewModel
@ExperimentalCoroutinesApi
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val repositoryJob: JobRepository,
    private val tokenManager: TokenManager,
    auth: AppAuth,
) : ViewModel() {
    private val cached = repository.data.cachedIn(viewModelScope)
    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?>
        get() = _imageUri
    private val _messageError = SingleLiveEvent<String>()
    val messageError: LiveData<String>
        get() = _messageError


    val data: Flow<PagingData<FeedItemEvent>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = event.authorId == myId)
                    } else {
                        event
                    }
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    val editedEvent: LiveData<Event>
        get() = edited
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media


    init {
        loadEvents()

    }

    fun loadEvents() = viewModelScope.launch {
        try {

            _dataState.value = FeedModelState(loading = true)
            repository.getEventsAll()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun changePhoto(file: File, uri: Uri?) {
        _media.value = MediaModel(uri, file)
    }

    fun clearPhoto() {
        _media.value = null
    }

    fun saveEvent() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    val token = tokenManager.getToken()
                    val jobs = repositoryJob.getJobAll(token)
                    val lastJob = jobs.firstOrNull()

                    if (lastJob != null) {
                        it.copy(authorJob = lastJob.name)
                    } else {
                        it.copy(authorJob = "")
                    }
                    when (val media = media.value) {
                        null -> repository.saveEvent(it)

                        else -> {
                            repository.saveEventWithAttachment(it, media)
                        }
                    }

                    _eventCreated.value = Unit
                    _dataState.value = FeedModelState()
                    Log.d(
                        "EventViewModel",
                        "Event saved with content: ${it.content} and link: ${it.link}"
                    )

                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }

            }
        }
        edited.value = empty
    }


    fun editEvent(eventId: Int) {
        viewModelScope.launch {
            try {
                val event = repository.getEvent(eventId)  // Get post by id from repository
                if (event != null) {
                    edited.value = event
                } else {
                    _messageError.value = "Event not found"
                }
            } catch (e: Exception) {
                _messageError.value = e.message
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changeDatetime(datetime: String) {
        val text = datetime.trim()
        if (edited.value?.datetime == text) {
            return
        }
        edited.value = edited.value?.copy(datetime = text)
    }

    fun changeType(type: String) {
        val text = type.trim()
        if (edited.value?.type == text) {
            return
        }
        edited.value = edited.value?.copy(type = text)
    }

    fun changeLink(link: String?) {
        val text = link?.trim()
        if (edited.value?.link == text) {
            return
        }
        edited.value = edited.value?.copy(link = text)
    }

    fun likeEventById(event: Event) {

        viewModelScope.launch {
            try {
                repository.likeEventById(event)

                _dataState.value = FeedModelState()


            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }

    }

    fun removeEventById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeEventById(id)

            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }
    }

    fun addParticipants(login: String, eventId: Int) {
        viewModelScope.launch {
            try {
                val userList = repository.userAll()
                val user = userList.find { it.login == login }

                if (user != null) {
                    val updatedEvent = repository.addParticipants(eventId)

                    val userIdInt = user.id.toInt()
                    if (updatedEvent.participantsIds?.contains(userIdInt) == false) {

                        val userPreview = UserPreview(user.name, user.avatar)
                        val updatedUsersMap = updatedEvent.users.toMutableMap()
                        updatedUsersMap[user.id.toLong()] = userPreview

                        val finalUpdatedEvent = updatedEvent.copy(users = updatedUsersMap)


                        repository.saveEvent(finalUpdatedEvent)

                        edited.value = finalUpdatedEvent

                    } else {
                        _messageError.value = "User is already a participant"
                    }
                } else {
                    _messageError.value = "User not found"
                }
            } catch (e: Exception) {
                _messageError.value = e.message
            }
        }
    }

    fun removeParticipant(name: String, eventId: Int) {
        viewModelScope.launch {
            try {
                val userList = repository.userAll()
                val userToRemove = userList.find { it.name == name }
                if (userToRemove != null) {
                    val updatedEvent = repository.removeParticipantById(eventId)
                    edited.value = updatedEvent
                } else {
                    _messageError.value = "User not found"
                }
            } catch (e: Exception) {
                _messageError.value = e.message
            }
        }
    }
}
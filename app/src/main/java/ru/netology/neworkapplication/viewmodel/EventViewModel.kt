package ru.netology.neworkapplication.viewmodel

import android.net.Uri
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
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.FeedItemEvent
import ru.netology.neworkapplication.dto.UserPreview
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.model.MediaModel
import ru.netology.neworkapplication.repository.events.EventRepository
import ru.netology.neworkapplication.repository.job.JobRepository
import ru.netology.neworkapplication.util.ResourceProvider
import ru.netology.neworkapplication.util.SingleLiveEvent
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
    participatedByMe = true,
    users = emptyMap()


)


@Suppress("unused")
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val repositoryJob: JobRepository,
    private val resourceProvider: ResourceProvider,
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
    private val eventWithParticipant = MutableLiveData(empty)
    val editedEventWithParticipant: LiveData<Event>
        get() = eventWithParticipant
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


    fun editEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                val event = repository.getEvent(eventId)

                edited.value = event

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

    fun removeEventById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeEventById(id)

            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }
    }

    fun getParticipantNamesForEvent(eventId: Long): List<String> {

        val event = edited.value
        if (event?.id == eventId) {

            return event.users.values.map { it.name }
        }
        return emptyList()
    }


    fun addParticipant(login: String) {
        viewModelScope.launch {
            try {
                val userList = repository.userAll()
                val user = userList.find { it.login == login }

                if (user != null) {
                    val userIdInt = user.id
                    val currentEvent = edited.value ?: empty
                    val currentParticipants = edited.value?.speakerIds ?: emptyList()
                    if (!currentParticipants.contains(userIdInt)) {
                        val updatedParticipants = currentParticipants + userIdInt
                        val userPreview = UserPreview(user.name, user.avatar)
                        val updatedUsersMap = currentEvent.users.toMutableMap()
                        updatedUsersMap[user.id] = userPreview
                        edited.value = currentEvent.copy(
                            speakerIds = updatedParticipants,
                            users = updatedUsersMap
                        )
                    } else {
                        _messageError.value =
                            resourceProvider.getString(R.string.already_participant)
                    }
                } else {
                    _messageError.value = resourceProvider.getString(R.string.user_not_found)
                }
            } catch (e: Exception) {
                _messageError.value = e.message
            }
        }
    }

    fun removeParticipantByName(name: String) {
        viewModelScope.launch {
            try {
                val userList = repository.userAll()
                val user = userList.find { it.name == name }

                if (user == null) {
                    _messageError.value = resourceProvider.getString(R.string.user_not_found)
                    return@launch
                }

                val userIdInt = user.id
                val currentEvent = edited.value ?: return@launch

                if (currentEvent.users.containsKey(userIdInt)) {
                    val updatedUsersMap = currentEvent.users.toMutableMap()
                    updatedUsersMap.remove(userIdInt)

                    val updatedParticipants = currentEvent.speakerIds?.filter { it != userIdInt }

                    edited.value = currentEvent.copy(
                        speakerIds = updatedParticipants,
                        users = updatedUsersMap
                    )
                } else {
                    _messageError.value = resourceProvider.getString(R.string.not_participant)
                }
            } catch (e: Exception) {
                _messageError.value =
                    e.message ?: resourceProvider.getString(R.string.error_occurred)
            }
        }
    }


    fun saveEvent() {

        edited.value?.let { currentEvent ->
            viewModelScope.launch {
                try {

                    val jobs = repositoryJob.getJobAll()
                    val lastJob = jobs.lastOrNull()

                    val updatedEvent = if (lastJob != null) {
                        currentEvent.copy(authorJob = lastJob.name)
                    } else {
                        currentEvent.copy(authorJob = "")
                    }

                    when (val media = media.value) {
                        null -> repository.saveEvent(updatedEvent)
                        else -> repository.saveEventWithAttachment(updatedEvent, media)
                    }

                    _dataState.value = FeedModelState()


                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }

            }
        }

        edited.value = empty

    }
}
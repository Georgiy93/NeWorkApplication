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
import okhttp3.MultipartBody
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.model.MediaModel
import ru.netology.neworkapplication.repository.PostRepository
import ru.netology.neworkapplication.util.SingleLiveEvent
import java.io.File

import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    authorId = 0,
    author = "",
    authorAvatar = "",
    authorJob = "",
    likedByMe = false,

    published = "",

    )



@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    private val cached = repository.data.cachedIn(viewModelScope)
    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?>
        get() = _imageUri
    private val _messageError = SingleLiveEvent<String>()
    val messageError: LiveData<String>
        get() = _messageError
    private val editedContent = MutableLiveData("")

    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    val editedPost: LiveData<Post>
        get() = edited
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media


    init {
        loadPosts()

    }

    fun loadPosts() = viewModelScope.launch {
        try {

            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
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

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    when (val media = media.value) {
                        null -> repository.save(it)
                        else -> {
                            repository.saveWithAttachment(it, media)
                        }
                    }

                    _postCreated.value = Unit
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }

            }
        }
        edited.value = empty
    }


    fun edit(postId: Int) {
        viewModelScope.launch {
            try {
                val post = repository.getPost(postId)  // Get post by id from repository
                if (post != null) {
                    edited.value = post
                } else {
                    _messageError.value = "Post not found"
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


    fun likeById(post: Post) {

        viewModelScope.launch {
            try {
                repository.likeById(post)

                _dataState.value = FeedModelState()


            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }

    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeById(id)

            } catch (e: Exception) {

                _messageError.value = e.message
            }
        }
    }

}
package ru.netology.neworkapplication.viewmodel

import android.net.Uri
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.model.FeedModelState
import ru.netology.neworkapplication.model.MediaModel
import ru.netology.neworkapplication.repository.wall.WallRepository
import ru.netology.neworkapplication.util.SingleLiveEvent
import ru.netology.neworkapplication.util.TokenManager
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class WallViewModel @Inject constructor(
    private val repository: WallRepository,
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


    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.filter { post ->
                    if (post is Post) {
                        post.authorId == tokenManager.getId()
                    } else {
                        false
                    }
                }
            }
        }


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


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

}
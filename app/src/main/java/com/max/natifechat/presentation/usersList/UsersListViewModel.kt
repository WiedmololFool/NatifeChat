package com.max.natifechat.presentation.usersList

import androidx.lifecycle.*
import com.max.natifechat.data.local.UserStorage
import com.max.natifechat.data.remote.ServerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import model.User

class UsersListViewModel(
    private val serverRepository: ServerRepository,
    private val userStorage: UserStorage
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    var loadUsersJob: Job? = null

    fun loadUsers() {
        serverRepository.apply {
            getUsersList().onEach { usersList ->
                _users.value = usersList
            }.launchIn(viewModelScope)
            loadUsersJob = viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    while (true) {
                        delay(1000)
                        requestUsers()
                    }
                }
            }
        }

    }

    suspend fun logout(): Boolean {
        loadUsersJob?.cancel()
        userStorage.clear()
        return serverRepository.disconnect()
    }
}
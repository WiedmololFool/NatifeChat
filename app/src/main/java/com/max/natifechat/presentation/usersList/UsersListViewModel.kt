package com.max.natifechat.presentation.usersList

import androidx.lifecycle.*
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.data.local.UserStorage
import kotlinx.coroutines.*
import model.User

class UsersListViewModel(
    private val serverRepository: ServerRepository,
    private val userStorage: UserStorage
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    var loadUsersJob: Job? = null

    init {
        viewModelScope.launch {
            serverRepository.getUsersList().collect { usersList ->
                _users.value = usersList
            }
        }
    }

    fun loadUsers() {
        loadUsersJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                serverRepository.requestUsers()
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            loadUsersJob?.cancel()
            userStorage.clear()
            serverRepository.disconnect()
        }
    }
}
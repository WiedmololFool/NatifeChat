package com.max.natifechat.presentation.usersList

import androidx.lifecycle.ViewModel
import com.max.natifechat.data.local.UserStorage
import com.max.natifechat.data.remote.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.User

class UsersListViewModel(
    private val serverRepository: ServerRepository,
    private val userStorage: UserStorage
) : ViewModel() {

    private val _users = MutableStateFlow(listOf<User>())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    suspend fun loadUsers() {
        _users.value = serverRepository.getUsers()
    }

    suspend fun logout(): Boolean {
        userStorage.clear()
        return serverRepository.disconnect()
    }
}
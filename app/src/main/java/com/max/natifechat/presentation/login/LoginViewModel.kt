package com.max.natifechat.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.log
import com.max.natifechat.presentation.login.model.ConnectionState
import com.max.natifechat.storage.UserStorage
import kotlinx.coroutines.*
import model.User

class LoginViewModel(
    private val serverRepository: ServerRepository,
    private val userStorage: UserStorage
) : ViewModel() {

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState
    private var isConnected = false

    init {
        viewModelScope.launch {
            serverRepository.getConnectStatus().collect { connectionStatus ->
                isConnected = connectionStatus
            }
        }
    }

    suspend fun performLogin(username: String) {
        log("performLogin")
        serverRepository.connectToServer(username)
        val job = withTimeoutOrNull(3000L) {
            while (!isConnected) {
                setConnectionState(ConnectionState.LOADING)
                delay(100)
            }
            setConnectionState(ConnectionState.SUCCESS)
            userStorage.save(User(id = serverRepository.getLoggedUserId(), name = username))
        }
        if (job == null) {
            setConnectionState(ConnectionState.ERROR)
        }

    }

    fun getUserFromStorage(): User {
        return userStorage.get()
    }

    private suspend fun setConnectionState(connectionState: ConnectionState) {
        withContext(Dispatchers.Main) {
            _connectionState.value = connectionState
        }
    }
}
package com.max.natifechat.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.max.natifechat.Constants
import com.max.natifechat.data.local.UserStorage
import com.max.natifechat.data.remote.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.User

class LoginViewModel(
    private val serverRepository: ServerRepository,
    private val userStorage: UserStorage
) : ViewModel() {

    private val _connectionStatus = MutableStateFlow(ConnectionStatus(false))
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    suspend fun performLogin(username: String) {
        serverRepository.apply {
            val connection = ConnectionStatus(connectToServer(username))
            _connectionStatus.value = connection
            Log.e(Constants.TAG, _connectionStatus.value.toString())
            if (connection.status){
                userStorage.save(User(id = getLoggedUserId(), name = username))
            }
        }
    }

    fun getUserFromStorage(): User {
        return userStorage.get()
    }

    data class ConnectionStatus(val status: Boolean) {

        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return status.hashCode()
        }
    }
}
package com.max.natifechat.data.remote

import com.max.natifechat.data.remote.model.IsConnected
import com.max.natifechat.data.remote.model.Message
import kotlinx.coroutines.flow.StateFlow
import model.User

interface ServerRepository {


    fun getServerIp(): String

    suspend fun connectToServer(username: String)

    fun getUsersList(): StateFlow<List<User>>

    suspend fun getReceivedMessages(senderId: String): StateFlow<List<Message>>

    suspend fun sendMessage(receiver: String, message: String)

    suspend fun requestUsers(): Boolean

    suspend fun disconnect(): Boolean

    fun getConnectStatus(): StateFlow<IsConnected>

    fun getLoggedUserId(): String

}
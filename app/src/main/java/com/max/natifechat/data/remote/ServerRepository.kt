package com.max.natifechat.data.remote

import model.MessageDto
import model.User

interface ServerRepository {

   fun getServerIp(): String

    suspend fun connectToServer(username: String): Boolean

    suspend fun getUsers(): List<User>

    suspend fun sendMessage(id: String, receiver: String, message: String)

    suspend fun newMessage(): MessageDto

    suspend fun disconnect(): Boolean

    fun getConnectionStatus(): Boolean

    fun getLoggedUserId(): String

}
package com.max.natifechat.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.max.natifechat.Constants
import com.max.natifechat.DateFormatter
import com.max.natifechat.data.remote.model.IsConnected
import com.max.natifechat.log
import com.max.natifechat.presentation.chat.models.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import model.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.time.LocalDateTime


class ServerRepositoryImpl : ServerRepository {

    private lateinit var socketHandler: SocketHandler
    private lateinit var userId: String
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val isConnected = MutableStateFlow(IsConnected(false))
    private val users = MutableStateFlow(listOf<User>())
    private val receivedMessages = MutableStateFlow(listOf<MessageDto>())
    private val dateFormatter = DateFormatter()
    private val chats = mutableListOf<Chat>()

    override fun getServerIp(): String {
        try {
            val serverDatagramSocket = DatagramSocket()
            val buffer = ByteArray(256)
            var datagramPacket =
                DatagramPacket(
                    buffer, buffer.size,
                    InetAddress.getByName(Constants.LOCALHOST), Constants.UDP_PORT
                )
            serverDatagramSocket.send(datagramPacket)
            datagramPacket = DatagramPacket(buffer, buffer.size)
            serverDatagramSocket.receive(datagramPacket)
            val serverIp = JSONObject(String(datagramPacket.data)).getString("ip")
            log(serverIp)
            return serverIp
        } catch (e: Exception) {
            log(e.message.toString())
            return "no ip"
        }
    }

    override suspend fun connectToServer(username: String) {
        try {
            socketHandler = SocketHandler(Socket(getServerIp(), Constants.TCP_PORT), coroutineScope)
//        connectJob = coroutineScope.launch(IO) {
//            while (true) {
            socketHandler.setListener(createClientListener(username = username))
            socketHandler.loop()
//            }
//        }
        } catch (e: Exception) {
            log(e.message.toString())
        }

    }

    private fun createClientListener(username: String): SocketHandler.SocketListener =
        object : SocketHandler.SocketListener {
            override fun onNewMessage(message: String) {
                try {
                    val dto = gson.fromJson(message, BaseDto::class.java)
                    when (dto.action) {
                        BaseDto.Action.CONNECTED -> {
                            handleConnected(dto.payload, username)
                        }
                        BaseDto.Action.PONG -> {
                            handlePong(dto.payload)
                        }
                        BaseDto.Action.NEW_MESSAGE -> {
                            handleNewMessage(dto.payload)
                        }
                        BaseDto.Action.USERS_RECEIVED -> {
                            handleUsersReceived(dto.payload)
                        }
                        else -> log("unknown action: $message")
                    }
                } catch (e: Exception) {
                    log("error handling message: ${e.message}")
                }
            }
        }


    private fun handleConnected(payload: String, username: String) {
        val connectedDto = gson.fromJson(payload, ConnectDto::class.java)
        userId = connectedDto.id
        socketHandler.send(BaseDto.Action.CONNECT, ConnectDto(userId, username))
        log("response Id = $userId")
        socketHandler.send(BaseDto.Action.PING, PingDto(userId))
        isConnected.value = IsConnected(true)
        log(isConnected.value.toString())
    }

    private fun handlePong(payload: String) {
        val pongDto = gson.fromJson(payload, PongDto::class.java)
        log(pongDto.toString())
        coroutineScope.launch(IO) {
            delay(5000L)
            log("send ping")
            socketHandler.send(BaseDto.Action.PING, PingDto(userId))
        }
    }

    private fun handleUsersReceived(payload: String) {
        val userReceivedDto = gson.fromJson(payload, UsersReceivedDto::class.java)
        val usersList = userReceivedDto.users.toMutableList().apply {
            removeAll {
                it.id == userId
            }
        }
        users.value = usersList.toList()
//        log(users.value.toString())
    }

    private fun handleNewMessage(payload: String) {
        val messageDto = gson.fromJson(payload, MessageDto::class.java)
        val currentReceivedMessages = receivedMessages.value
        receivedMessages.value = currentReceivedMessages + listOf(messageDto)
        log("HANDLE NEW MESSAGE FROM ${messageDto.from}: ${messageDto.message}")
        log("HANDLE LIST OF MESSAGES: \n ${receivedMessages.value}")
    }

    override fun getUsersList(): StateFlow<List<User>> {
        return users.asStateFlow()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getReceivedMessages(senderId: String): StateFlow<List<Message>> {
//        val list = receivedMessages.value.toMutableList().apply {
//            removeAll {
//                it.from.id != senderId
//            }
//        }
        val date = dateFormatter.format(LocalDateTime.now())
        return receivedMessages.asStateFlow().map { list ->
            list.toMutableList().apply {
                removeAll {
                    it.from.id != senderId
                }
            }.toList().map {
                Message(senderId, message = it.message, date = date, false)
            }
        }.stateIn(scope = coroutineScope)

//        return receivedMessages.asStateFlow()
    }

    override suspend fun requestUsers(): Boolean {
        socketHandler.send(BaseDto.Action.GET_USERS, GetUsersDto(userId))
        return true
    }

    override suspend fun sendMessage(receiver: String, message: String) {
        Log.e(Constants.TAG, "repository send message $message")
        socketHandler.send(
            BaseDto.Action.SEND_MESSAGE, SendMessageDto(
                id = userId,
                receiver = receiver,
                message = message
            )
        )
    }

    override suspend fun disconnect(): Boolean {
        isConnected.value = IsConnected(false)
        socketHandler.apply {
            send(BaseDto.Action.DISCONNECT, DisconnectDto(userId, 404))
            disconnect()
        }
        return true
    }

    override fun getConnectStatus(): StateFlow<IsConnected> {
        log(isConnected.value.toString())
        return isConnected.asStateFlow()
    }

    override fun getLoggedUserId(): String {
        return userId
    }

    data class Chat(val chatId: String, val messages: MutableStateFlow<Message>)
}

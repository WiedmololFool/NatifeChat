package com.max.natifechat.data.remote

import android.util.Log
import com.google.gson.Gson
import com.max.natifechat.Constants
import com.max.natifechat.DateFormatter
import com.max.natifechat.log
import com.max.natifechat.data.remote.model.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import model.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.util.*


class ServerRepositoryImpl : ServerRepository {

    private var socketHandler: SocketHandler? = null
    private var userId: String? = null
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val isConnected = MutableStateFlow(false)
    private val users = MutableStateFlow(listOf<User>())
    private val receivedMessages = MutableStateFlow(listOf<MessageDto>())
    private val dateFormatter = DateFormatter()
    private val chats = mutableListOf<Chat>()
    private var pingTimeoutJob: Job? = null

    init {
        log("serverRepository init")
        log("chats init $chats")
        createChatForNewUsers()
    }

    private fun getServerIp(): String {
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
            return Constants.NO_IP
        }
    }

    override suspend fun connectToServer(username: String) {
        try {
            socketHandler = SocketHandler(
                Socket(getServerIp(), Constants.TCP_PORT),
                coroutineScope,
                createClientListener(username = username)
            )
            socketHandler?.loop()
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
        log("handle connected")
        val connectedDto = gson.fromJson(payload, ConnectDto::class.java)
        userId = connectedDto.id.also { userId ->
            log("response Id = $userId")
            socketHandler?.send(BaseDto.Action.CONNECT, ConnectDto(userId, username))
            socketHandler?.send(BaseDto.Action.PING, PingDto(userId))
            isConnected.value = true
            log(isConnected.value.toString())
            coroutineScope.launch(IO) {
                while (isConnected.value) {
                    delay(5000L)
                    log("send ping")
                    socketHandler?.send(BaseDto.Action.PING, PingDto(userId))
                    pingTimeoutJob = this.launch {
                        log("PING TIMEOUT JOB IS STARTED")
                        delay(10000L)
                        log("PING TIMEOUT")
                        disconnect()
                    }
                }
            }
        }
    }

    private fun handlePong(payload: String) {
        val pongDto = gson.fromJson(payload, PongDto::class.java)
        log(pongDto.toString())
        pingTimeoutJob?.cancel()
    }

    private fun handleUsersReceived(payload: String) {
        val userReceivedDto = gson.fromJson(payload, UsersReceivedDto::class.java)
        users.value = userReceivedDto.users
    }


    private fun handleNewMessage(payload: String) {
        val date = dateFormatter.format(Date())
        val messageDto = gson.fromJson(payload, MessageDto::class.java)
        val currentReceivedMessages = receivedMessages.value
        receivedMessages.value = currentReceivedMessages + listOf(messageDto)
        log("HANDLE NEW MESSAGE FROM ${messageDto.from}: ${messageDto.message}")
        log("HANDLE LIST OF MESSAGES: \n ${receivedMessages.value}")
        val chat = chats.firstOrNull {
            it.chatId == messageDto.from.id
        }
        log("FINDED CHAT IN HANDLE NEW MESSAGE $chat")
        val currentMessages = chat?.messages?.value
        if (currentMessages != null) {
            chat.messages.value = currentMessages + listOf(
                Message(
                    chatId = messageDto.from.id,
                    message = messageDto.message,
                    date = date, false
                )
            )
        }
    }

    private fun createChatForNewUsers() {
        users.asStateFlow().onEach { userList ->
            log("ON UPDATE USERS $userList")
            userList.forEach { user ->
                if (!chats.any { chat ->
                        chat.chatId == user.id
                    }) {
                    chats.add(Chat(user.id, MutableStateFlow(listOf<Message>())))
                    log("CHATLIST UPDATED $chats")
                }
            }
        }.launchIn(scope = coroutineScope)
    }

    override fun getUsersList(): StateFlow<List<User>> {
        return users.asStateFlow()
    }

    override fun getUserById(userId: String): User {
        return users.value.firstOrNull {
            it.id == userId
        } ?: throw java.lang.IllegalArgumentException("User required")
    }

    override fun getReceivedMessages(senderId: String): StateFlow<List<Message>> {
        val chat = chats.firstOrNull {
            it.chatId == senderId
        }
        return chat?.messages?.asStateFlow() ?: throw IllegalArgumentException("Flow required")
    }


    override suspend fun requestUsers(): Boolean {
        socketHandler?.send(BaseDto.Action.GET_USERS, GetUsersDto(userId!!))
        return true
    }


    override suspend fun sendMessage(receiverId: String, message: String) {
        val date = dateFormatter.format(Date())
        Log.e(Constants.TAG, "repository send message $message")
        socketHandler?.send(
            BaseDto.Action.SEND_MESSAGE, SendMessageDto(
                id = userId!!,
                receiver = receiverId,
                message = message
            )
        )
        val chat = chats.firstOrNull {
            it.chatId == receiverId
        }
        log("FINDED CHAT IN SEND MESSAGE $chat")
        val currentMessages = chat?.messages?.value
        if (currentMessages != null) {
            chat.messages.value = currentMessages + listOf(
                Message(receiverId, message, date, true)
            )
        }
    }

    override suspend fun disconnect(): Boolean {
        chats.clear()
        isConnected.value = false
        socketHandler?.apply {
            send(BaseDto.Action.DISCONNECT, DisconnectDto(userId!!, 404))
            disconnect()
        }
        log("Disconnect")
        return true
    }

    override fun getConnectStatus(): StateFlow<Boolean> {
        log(isConnected.value.toString())
        return isConnected.asStateFlow()
    }

    override fun getLoggedUserId(): String {
        return userId ?: throw java.lang.IllegalArgumentException("userId is required")
    }

    data class Chat(val chatId: String, val messages: MutableStateFlow<List<Message>>)
}

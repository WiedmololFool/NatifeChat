package com.max.natifechat.data.remote

import android.util.Log
import com.google.gson.Gson
import com.max.natifechat.Constants
import com.max.natifechat.log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import model.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket


class ServerRepositoryImpl : ServerRepository {

    private lateinit var socketHandler: SocketHandler
    private lateinit var userId: String
    private val gson = Gson()
    private var pongJob: Job? = null
    private var connectJob: Job? = null
    private var usersList = mutableListOf<User>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var handleUsersReceivedJob: CompletableJob
    private var handleConnectedJob: CompletableJob = Job()

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
            Log.e(Constants.TAG, serverIp)
            return serverIp
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message.toString())
            return "no ip"
        }
    }

    override suspend fun connectToServer(username: String): Boolean {
        socketHandler = SocketHandler(Socket(getServerIp(), Constants.TCP_PORT), coroutineScope)
        connectJob = coroutineScope.launch(IO) {
            while (true) {
                socketHandler.setListener(createClientListener(username = username))
                socketHandler.loop()
            }
        }
        while (handleConnectedJob.isActive) {
            log("handle connected job is Active, Wait...")
        }
        log("handle connected job is Completed")
        return getConnectionStatus()
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

                        }
                        BaseDto.Action.USERS_RECEIVED -> {
                            handleUsersReceived(dto.payload)
                        }
                        else -> Log.e(Constants.TAG, "unknown action: $message")
                    }
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "error handling message: ${e.message}")
                }
            }
        }

    private fun handleConnected(payload: String, username: String) {
        coroutineScope.launch(IO + handleConnectedJob) {
            val connectedDto = gson.fromJson(payload, ConnectDto::class.java)
            userId = connectedDto.id
            socketHandler.send(BaseDto.Action.CONNECT, ConnectDto(userId, username))
            log("response Id = $userId")
            socketHandler.send(BaseDto.Action.PING, PingDto(userId))
            handleConnectedJob.complete()
        }
    }

    private fun handlePong(payload: String) {
        val pongDto = gson.fromJson(payload, PongDto::class.java)
        log(pongDto.toString())
        CoroutineScope(IO).launch {
            delay(1000)
            log("send ping")
            socketHandler.send(BaseDto.Action.PING, PingDto(userId))
        }
    }


    private fun handleUsersReceived(payload: String) {
        val userReceivedDto = gson.fromJson(payload, UsersReceivedDto::class.java)
        usersList = userReceivedDto.users.toMutableList().apply {
            removeAll {
                it.id == userId
            }
        }
    }

    override suspend fun getUsers(): List<User> {
        socketHandler.send(BaseDto.Action.GET_USERS, GetUsersDto(userId))
        log(usersList.toList().toString())
        return usersList.toList()
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

    override suspend fun newMessage(): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        pongJob?.cancel()
        connectJob?.cancel()
        socketHandler.apply {
            send(BaseDto.Action.DISCONNECT, DisconnectDto(userId, 404))
            disconnect()
        }
        return true
    }

    override fun getConnectionStatus(): Boolean {
        return socketHandler.getConnectionStatus()
    }

    override fun getLoggedUserId(): String {
        return userId
    }

}

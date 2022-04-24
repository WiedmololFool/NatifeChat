package com.max.natifechat.data.remote

import android.util.Log
import com.google.gson.Gson
import com.max.natifechat.Constants
import kotlinx.coroutines.*
import model.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket


class ServerRepositoryImpl : ServerRepository {

    private val gson = Gson()
//
//    private val socketHandler: SocketHandler by lazy {
//        Log.e(Constants.TAG, "Socket handler init")
//        SocketHandler(Socket(getServerIp(), Constants.TCP_PORT))
//    }

    private var socketHandler: SocketHandler? = null

    private val userId by lazy {
        val baseDto = socketHandler?.read()
        val connectedDto = baseDto?.getPayloadClass() as ConnectedDto
        Log.e(Constants.TAG, "response Id = ${connectedDto.id}")
        connectedDto.id
    }

    var job: Job? = null

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
        socketHandler = SocketHandler(Socket(getServerIp(), Constants.TCP_PORT))
        try {
            socketHandler?.apply {
                send(BaseDto.Action.CONNECT, ConnectDto(userId, username))
                job = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        delay(5000)
                        send(BaseDto.Action.PING, ConnectedDto(userId))
                        //Log PONG
                        Log.e(Constants.TAG, read().toString())
                    }
                }
                Log.e(
                    Constants.TAG,
                    "ServerRepositoryImpl connection status ${serverIsConnected()}"
                )
                return getConnectionStatus()
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message.toString())
            return false
        }
        return getConnectionStatus()
    }

    override suspend fun getUsers(): List<User> {
        var userList = listOf<User>()
        socketHandler?.apply {
            send(BaseDto.Action.GET_USERS, GetUsersDto(userId))
            userList = (read().getPayloadClass() as UsersReceivedDto).users.apply {
                toMutableList().removeAll {
                    it.id == userId
                }
            }
        }
        return userList
    }

    override suspend fun sendMessage(id: String, receiver: String, message: String) {
        TODO("Not yet implemented")
    }

    override suspend fun newMessage(): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        job?.cancel()
        socketHandler?.apply {
            send(BaseDto.Action.DISCONNECT, DisconnectDto(userId, 404))
            disconnect()
        }
        return true
    }

    override fun getConnectionStatus(): Boolean {
        return socketHandler?.serverIsConnected() ?: false
    }

    override fun getLoggedUserId(): String {
        return userId
    }


    private fun BaseDto.getPayloadClass(): Payload {

        when (this.action) {
            BaseDto.Action.PONG -> {
                return gson.fromJson(this.payload, PongDto::class.java)
            }
            BaseDto.Action.CONNECTED -> {
                return gson.fromJson(this.payload, ConnectedDto::class.java)
            }
            BaseDto.Action.NEW_MESSAGE -> {
                return gson.fromJson(this.payload, MessageDto::class.java)
            }
            BaseDto.Action.USERS_RECEIVED -> {
                return gson.fromJson(this.payload, UsersReceivedDto::class.java)
            }
            BaseDto.Action.DISCONNECT -> {

            }
            else -> Log.e(Constants.TAG, "unknown action: ${this.action}")
        }
        return Error("Unknown action")
    }
}
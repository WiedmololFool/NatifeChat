package com.max.natifechat.data.remote

import android.util.Log
import com.max.natifechat.Constants
import com.max.natifechat.getPayloadClass
import kotlinx.coroutines.*
import model.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket


class ServerRepositoryImpl : ServerRepository {

    private lateinit var socketHandler: SocketHandler
    private lateinit var userId: String

    private var job: Job? = null

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
            socketHandler.apply {
                val baseDto = read()
                val connectedDto = baseDto.getPayloadClass() as ConnectedDto
                userId = connectedDto.id
                Log.e(Constants.TAG, "response Id = $userId")
                send(BaseDto.Action.CONNECT, ConnectDto(userId, username))
                job = CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        delay(5000)
                        send(BaseDto.Action.PING, ConnectedDto(userId))
                        //Log PONG
//                        socketHandler.setOnClickListener {
//                            when (it.action) {
//                                BaseDto.Action.PING -> {
//                                    Log.e(Constants.TAG, read().toString())
//                                }
//                                else -> Log.e(Constants.TAG, "unknown action")
//                            }
//                        }
                        Log.e(Constants.TAG, read().toString())
                    }
                }
                Log.e(
                    Constants.TAG,
                    "ServerRepositoryImpl connection status ${serverIsConnected()}"
                )
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message.toString())
        }
        return getConnectionStatus()
    }

    override suspend fun getUsers(): List<User> {

        socketHandler.apply {
            send(BaseDto.Action.GET_USERS, GetUsersDto(userId))

            return (read().getPayloadClass() as UsersReceivedDto).users.apply {
                toMutableList().removeAll {
                    it.id == userId
                }
            }
        }
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
        job?.cancel()
        socketHandler.apply {
            send(BaseDto.Action.DISCONNECT, DisconnectDto(userId, 404))
            disconnect()
        }
        return true
    }

    override fun getConnectionStatus(): Boolean {
        return socketHandler.serverIsConnected()
    }

    override fun getLoggedUserId(): String {
        return userId
    }



}


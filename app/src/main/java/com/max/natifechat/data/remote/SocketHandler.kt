package com.max.natifechat.data.remote

import android.util.Log
import com.google.gson.Gson
import com.max.natifechat.Constants
import model.BaseDto
import model.Payload
import model.User
import java.io.*
import java.net.Socket

class SocketHandler(
    private val server: Socket
) {
    private val reader = BufferedReader(InputStreamReader(server.getInputStream()))
    private val writer = PrintWriter(OutputStreamWriter(server.getOutputStream()))
    private val gson = Gson()
    private var connected = true
//    private lateinit var onMessageListener: (BaseDto) -> Unit

    fun send(action: BaseDto.Action, payload: Payload) {
        writer.println(gson.toJson(BaseDto(action, gson.toJson(payload))))
        writer.flush()
    }

    fun read(): BaseDto {
        val data = gson.fromJson(reader.readLine(), BaseDto::class.java)
//        onMessageListener.invoke(data)
        return data
    }

    fun serverIsConnected(): Boolean {
        Log.e(Constants.TAG, "server is connected ${connected}")
        return server.isConnected
    }

//    fun setOnClickListener(listener: (BaseDto) -> Unit) {
//        onMessageListener = listener
//    }

    fun disconnect() {
        connected = false
        reader.close()
        writer.close()
        server.close()
    }


}
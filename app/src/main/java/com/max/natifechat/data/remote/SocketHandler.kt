package com.max.natifechat.data.remote

import com.google.gson.Gson
import com.max.natifechat.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.BaseDto
import model.Payload
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException

class SocketHandler(
    private val server: Socket,
    private val scope: CoroutineScope,
    private val listener: SocketListener
) {
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter
    private val gson = Gson()
    private var connected = true
    private lateinit var loopJob: Job

    init {
        server.soTimeout = 6000
    }

    fun send(action: BaseDto.Action, payload: Payload) {
        writer.println(gson.toJson(BaseDto(action, gson.toJson(payload))))
        writer.flush()
    }

    private fun read() {
        try {
            val data = reader.readLine()
            listener.onNewMessage(data)
        } catch (io: IOException) {
            log(io.message.toString())
            if (!io.javaClass.isAssignableFrom(SocketTimeoutException::class.java)) {
                disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loop() {
        loopJob = scope.launch(Dispatchers.IO) {
            reader = BufferedReader(InputStreamReader(server.getInputStream()))
            writer = PrintWriter(OutputStreamWriter(server.getOutputStream()))
            while (connected) {
                read()
            }
        }
    }

    fun disconnect() {
        connected = false
        loopJob.cancel()
        reader.close()
        writer.close()
        server.close()
    }

    interface SocketListener {
        fun onNewMessage(message: String)
    }
}
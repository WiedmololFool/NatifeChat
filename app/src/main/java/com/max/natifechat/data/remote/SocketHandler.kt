package com.max.natifechat.data.remote

import android.util.Log
import com.google.gson.Gson
import com.max.natifechat.Constants
import com.max.natifechat.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.BaseDto
import model.Payload
import java.io.*
import java.net.Socket

class SocketHandler(
    private val server: Socket,
    private val scope: CoroutineScope
) {
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter
    private val gson = Gson()
    private var connected = true
    private lateinit var loopJob: Job
    private lateinit var listener: SocketListener


    fun send(action: BaseDto.Action, payload: Payload) {
        writer.println(gson.toJson(BaseDto(action, gson.toJson(payload))))
        writer.flush()
    }

    fun read() {
        try {
            val data = reader.readLine()
            listener.onNewMessage(data)
        } catch (io: IOException) {
            disconnect()
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

    fun getConnectionStatus(): Boolean {
        Log.e(Constants.TAG, "server is connected ${connected}")
        return server.isConnected
    }

    fun setListener(listener: SocketListener) {
        this.listener = listener
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
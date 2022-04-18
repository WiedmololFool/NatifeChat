package com.max.natifechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.BaseDto
import model.ConnectDto
import model.ConnectedDto
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv: TextView = findViewById(R.id.tv)
        tv.setOnClickListener {
            Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show()
            Log.e(Constants.TAG, "Click")
            CoroutineScope(IO).launch {
                connectToServer(getServerIp())
            }
        }
    }

    private suspend fun getServerIp(): String {
        try {
            val serverDatagramSocket = DatagramSocket()
            val buffer = ByteArray(256)
            var datagramPacket =
                DatagramPacket(
                    buffer, buffer.size,
                    InetAddress.getByName("255.255.255.255"), 8888
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

    private suspend fun connectToServer(serverIp: String) {
        val serverSocket = Socket(serverIp, 6666)
        val reader = BufferedReader(InputStreamReader(serverSocket.getInputStream()))
        val writer = PrintWriter(OutputStreamWriter(serverSocket.getOutputStream()))
        try {
            val dto = gson.fromJson(reader.readLine(), BaseDto::class.java)
            val connectedDto = gson.fromJson(dto.payload, ConnectedDto::class.java)

            val connectDto = gson.toJson(
                BaseDto(
                    BaseDto.Action.CONNECT,
                    gson.toJson(ConnectDto(connectedDto.id, "Max"))
                )
            )
            writer.println(connectDto)
            Log.e(Constants.TAG, "response = ${connectedDto.id}")
                while (true) {
                    delay(1000)
                    writer.println(
                        gson.toJson(
                            BaseDto(
                                BaseDto.Action.PING,
                                gson.toJson(ConnectedDto(connectedDto.id))
                            )
                        )
                    )
                }
            writer.flush()

        } catch (e: JSONException) {
            Log.e(Constants.TAG, e.message.toString())
        }


    }
}
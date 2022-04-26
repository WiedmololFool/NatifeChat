package com.max.natifechat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateFormatter {

    @RequiresApi(Build.VERSION_CODES.O)
    private val format = DateTimeFormatter.ofPattern("HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.O)
    fun format(date: LocalDateTime): String {
        return format.format(date)
    }

}

private val gson = Gson()

fun BaseDto.getPayloadClass(): Payload {

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

fun log(input: String) {
    var retVal = ""
    val trace = Throwable().stackTrace
    if (trace.size >= 2) {
        val index = trace[1].className.lastIndexOf(".") + 1
        retVal = (trace[1].className.substring(index) + "." + trace[1].methodName
                + "()")
    }
    Log.e("${Constants.TAG} $retVal", input)
}
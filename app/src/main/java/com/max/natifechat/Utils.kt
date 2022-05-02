package com.max.natifechat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateFormatter {

    @RequiresApi(Build.VERSION_CODES.O)
    private val format = DateTimeFormatter.ofPattern("HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    fun format(date: LocalDateTime): String {
        return format.format(date)
    }

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
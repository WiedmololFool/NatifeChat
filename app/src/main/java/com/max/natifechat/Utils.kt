package com.max.natifechat

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {

    fun format(date: Date): String {
        return SimpleDateFormat("HH:mm").format(date)
    }

}

fun log(input: String) {
    var retVal = ""
    val trace = Thread.currentThread().stackTrace
    if (trace.size >= 2) {
        val index = trace[3].className.lastIndexOf(".") + 1
        retVal = (trace[3].className.substring(index) + "." + trace[3].methodName + "()")
    }
    Log.e("${Constants.TAG} $retVal", input)
}
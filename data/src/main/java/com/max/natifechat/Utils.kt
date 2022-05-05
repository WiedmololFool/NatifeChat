package com.max.natifechat

import java.text.SimpleDateFormat
import java.util.*

internal class DateFormatter {

    fun format(date: Date): String {
        return SimpleDateFormat("HH:mm").format(date)
    }

}

internal fun log(input: String) {
    var retVal = ""
    val trace = Thread.currentThread().stackTrace
    if (trace.size >= 2) {
        val index = trace[3].className.lastIndexOf(".") + 1
        retVal = (trace[3].className.substring(index) + "." + trace[3].methodName + "()")
    }
    println("${Constants.TAG} $retVal $input")
}
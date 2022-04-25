package com.max.natifechat.presentation.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.max.natifechat.DateFormatter
import com.max.natifechat.data.remote.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class ChatViewModel(
    private val receiverId: String,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val dateFormatter = DateFormatter()

    @RequiresApi(Build.VERSION_CODES.O)
    val date = dateFormatter.format(LocalDateTime.now())

    suspend fun sendMessage(message: String) {
        serverRepository.sendMessage(receiverId, message)
        val currentMessages = _messages.value
        val message = Message(message = message, date = date)
        _messages.value = currentMessages + listOf(message)

    }

    data class Message(val message: String, val date: String)
}
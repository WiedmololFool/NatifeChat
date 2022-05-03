package com.max.natifechat.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.log
import com.max.natifechat.data.remote.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.User
import java.lang.Exception

class ChatViewModel(
    private val receiverId: String,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    init {
        loadMessages()

    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.sendMessage(receiverId, message)
        }
    }

    fun loadReceiver(): User {
        return serverRepository.getUserById(receiverId)
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                serverRepository.getReceivedMessages(receiverId).collect { messageList ->
                    log("MESSAGES IN VIEW MODEL $messageList")
                    _messages.value = messageList
                }

            } catch (e: Exception) {
                log(e.message.toString())
            }
        }
    }
}
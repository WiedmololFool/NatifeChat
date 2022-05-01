package com.max.natifechat.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.log
import com.max.natifechat.data.remote.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception

class ChatViewModel(
    private val receiverId: String,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.sendMessage(receiverId, message)
        }
    }

    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                serverRepository.getReceivedMessages(receiverId).onEach { messageList ->
                    log("MESSAGES IN VIEW MODEL $messageList")
                    _messages.value = messageList
                }.launchIn(viewModelScope)
            } catch (e: Exception) {
                log(e.message.toString())
            }

        }
    }
}
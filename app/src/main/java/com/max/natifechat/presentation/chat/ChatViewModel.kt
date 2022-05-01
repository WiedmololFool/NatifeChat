package com.max.natifechat.presentation.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.natifechat.DateFormatter
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.log
import com.max.natifechat.presentation.chat.models.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ChatViewModel(
    private val receiverId: String,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val dateFormatter = DateFormatter()

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(message: String) {

        val date = dateFormatter.format(LocalDateTime.now())

        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.sendMessage(receiverId, message)
        }
        val currentMessages = ChatHolder.getChat(receiverId)
        _messages.value =
            currentMessages + listOf(Message(receiverId, message = message, date = date, true))
        ChatHolder.list = _messages.value ?: listOf()
    }

    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.getReceivedMessages(receiverId).onEach { receivedMessagesList ->
                log("TRIGGER $receivedMessagesList")
                val currentMessages = ChatHolder.getChat(receiverId)
                log("CURRENT MESSAGES FROM CHAT HOLDER $currentMessages")
                if (receivedMessagesList.isNotEmpty()) {
                    val lastReceiveMessage = receivedMessagesList.last()
                    log(lastReceiveMessage.toString())
                    if (currentMessages.isNotEmpty()) {
                        val receivedMessagesFromCache = currentMessages.toMutableList().apply {
                            removeAll {
                                it.fromMe
                            }
                        }.toList()
                        if (receivedMessagesFromCache.isNotEmpty()){
                            val lastReceiveMessageFromCache = receivedMessagesFromCache.last()
                            if (lastReceiveMessageFromCache != lastReceiveMessage) {
                                log("NOT EQUALS LAST \n " +
                                        "lastReceiveMessageFromCache: ${lastReceiveMessageFromCache} \n" +
                                        "lastReceiveMessage: $lastReceiveMessage")
                                _messages.value = currentMessages + lastReceiveMessage
                                ChatHolder.list = _messages.value ?: listOf()
                            } else {
                                log("LAST MESSAGE IS EQUALS")
                                _messages.value = currentMessages
                            }
                        } else{
                            _messages.value = currentMessages + lastReceiveMessage
                            ChatHolder.list = _messages.value ?: listOf()
                        }
                    } else  {
                        log("CURRENT MESSAGES IS EMPTY")
                        _messages.value = currentMessages + lastReceiveMessage
                        ChatHolder.list = _messages.value ?: listOf()
                    }

//                    _messages.value = currentMessages + receivedMessagesList.map {
//                        Message(receiverId, message = it.message, date = date, false)
//                    }.last()
//                    ChatHolder.list = _messages.value ?: listOf()
                } else {
                    _messages.value = currentMessages
                }
            }.launchIn(viewModelScope)
        }
    }


}
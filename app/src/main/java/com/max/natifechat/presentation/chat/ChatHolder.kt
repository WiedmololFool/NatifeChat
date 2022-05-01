package com.max.natifechat.presentation.chat

import com.max.natifechat.presentation.chat.models.Message
import model.User

object ChatHolder {
    var list: List<Message> = listOf()

    fun getChat(receiverId: String): List<Message> {
        return list.toMutableList().apply {
            removeAll {
                it.chatId != receiverId
            }
        }.toList()
    }

    fun getReceivedMessages(){

    }
}
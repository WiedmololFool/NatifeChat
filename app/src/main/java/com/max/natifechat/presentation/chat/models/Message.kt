package com.max.natifechat.presentation.chat.models


/**
 * chatID represents an id of sobesednik
 */
data class Message(val chatId: String, val message: String, val date: String, val fromMe: Boolean)
package com.max.natifechat.data.remote.model


/**
 * chatID represents an id of sobesednik
 */
data class Message(val chatId: String, val message: String, val date: String, val fromMe: Boolean)
package com.max.natifechat.presentation.usersList

import model.User

object UsersHolder {

    var list: List<User> = listOf()

    fun getUser(id: String): User {
        return list.firstOrNull() { it.id == id } ?: throw IllegalArgumentException("User required")
    }
}
package com.max.natifechat.presentation.usersList

import model.User

object UsersHolder {

    var list: List<User> = listOf()

    fun getUser(id: String): User? {
        return list.find { it.id == id }
    }
}
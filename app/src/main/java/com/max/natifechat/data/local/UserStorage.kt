package com.max.natifechat.data.local

import model.User

interface UserStorage {

    fun save(user: User): Boolean

    fun get(): User

    fun clear(): Boolean
}
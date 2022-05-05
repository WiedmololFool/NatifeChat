package com.max.natifechat.storage

import model.User

interface UserStorage {

    fun save(user: User): Boolean

    fun get(): User

    fun clear(): Boolean
}
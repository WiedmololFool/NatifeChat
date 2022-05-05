package com.max.natifechat.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.max.natifechat.Constants
import com.max.natifechat.data.local.UserStorage
import com.max.natifechat.log
import model.User


internal class SharedPrefUserStorage(private val sharedPreferences: SharedPreferences) :
    UserStorage {

    override fun save(user: User): Boolean {
        Constants.apply {
            sharedPreferences.edit {
                putString(KEY_USER_ID, user.id).apply()
                putString(KEY_USER_NAME, user.name).apply()
            }
            log("Saved user $user")
            return true
        }
    }

    override fun get(): User {
        Constants.apply {
            val id = sharedPreferences.getString(KEY_USER_ID, DEFAULT_USER_ID) ?: DEFAULT_USER_ID
            val name =
                sharedPreferences.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME
            return User(id = id, name = name)
        }
    }

    override fun clear(): Boolean {
        sharedPreferences.edit().clear().apply()
        return true
    }
}
package com.max.natifechat.data.local

import android.content.Context
import android.util.Log
import com.max.natifechat.Constants
import model.User


class SharedPrefUserStorage(context: Context) : UserStorage {

    private val sharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE
    )

    override fun save(user: User): Boolean {
        Constants.apply {
            sharedPreferences.edit().putString(KEY_USER_ID, user.id).apply()
            sharedPreferences.edit().putString(KEY_USER_NAME, user.name).apply()
            Log.e(TAG, "Saved user $user")
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
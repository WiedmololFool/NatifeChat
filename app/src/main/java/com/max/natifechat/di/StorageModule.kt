package com.max.natifechat.di

import android.content.Context
import android.content.SharedPreferences
import com.max.natifechat.Constants
import com.max.natifechat.storage.SharedPrefUserStorage
import com.max.natifechat.storage.UserStorage
import org.koin.dsl.module

val storageModule = module {
    single<UserStorage> {
        SharedPrefUserStorage(sharedPreferences = get())
    }

    factory<SharedPreferences> {
        get<Context>().getSharedPreferences(
            Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE
        )
    }
}
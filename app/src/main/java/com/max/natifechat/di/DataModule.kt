package com.max.natifechat.di

import android.content.Context
import android.content.SharedPreferences
import com.max.natifechat.Constants
import com.max.natifechat.data.local.SharedPrefUserStorage
import com.max.natifechat.data.local.UserStorage
import com.max.natifechat.data.remote.ServerRepository
import com.max.natifechat.data.remote.ServerRepositoryImpl
import org.koin.dsl.module

val dataModule = module {

    single<ServerRepository> {
        ServerRepositoryImpl()
    }

    single<UserStorage> {
        SharedPrefUserStorage(sharedPreferences = get())
    }

    factory<SharedPreferences> {
        get<Context>().getSharedPreferences(
            Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE
        )
    }
}
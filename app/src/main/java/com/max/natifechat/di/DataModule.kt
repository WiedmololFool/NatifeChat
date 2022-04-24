package com.max.natifechat.di

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
        SharedPrefUserStorage(context = get())
    }
}
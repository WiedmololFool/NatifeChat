package com.max.natifechat.di

import com.max.natifechat.presentation.chat.ChatViewModel
import com.max.natifechat.presentation.login.LoginViewModel
import com.max.natifechat.presentation.usersList.UsersListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel {
        LoginViewModel(serverRepository = get(), userStorage = get())
    }

    viewModel {
        UsersListViewModel(serverRepository = get(), userStorage = get())
    }

    viewModel { (receiverId: String) ->
        ChatViewModel(serverRepository = get(), receiverId = receiverId)
    }
}
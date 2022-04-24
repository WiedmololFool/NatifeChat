package com.max.natifechat.presentation.login

import androidx.lifecycle.lifecycleScope
import com.max.natifechat.presentation.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.User
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseLoginFragment() : BaseFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    protected fun login(
        username: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {

        viewModel.connectionStatus.drop(1).onEach { connection ->
            withContext(Dispatchers.Main) {
                if (connection.status) {
                    onSuccess.invoke()
                } else {
                    onError.invoke()
                }
            }
        }.launchIn(lifecycleScope)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.performLogin(username = username)
        }
    }

    protected fun getUserFromStorage(): User {
        return viewModel.getUserFromStorage()
    }

}


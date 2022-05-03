package com.max.natifechat.presentation.login

import androidx.lifecycle.lifecycleScope
import com.max.natifechat.log
import com.max.natifechat.presentation.BaseFragment
import com.max.natifechat.presentation.login.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.User
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Exception

abstract class BaseLoginFragment() : BaseFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    protected fun login(
        username: String,
        onSuccess: () -> Unit,
        onLoading: ()-> Unit,
        onError: () -> Unit
    ) {

        viewModel.connectionState.observe(viewLifecycleOwner) { connectionState ->
            when (connectionState) {
                ConnectionState.SUCCESS -> onSuccess.invoke()
                ConnectionState.LOADING -> onLoading.invoke()
                ConnectionState.ERROR -> onError.invoke()
                else -> log("Unknown connectionState")
            }
        }


           viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
               viewModel.performLogin(username = username)
           }
    }

    protected fun getUserFromStorage(): User {
        return viewModel.getUserFromStorage()
    }
}

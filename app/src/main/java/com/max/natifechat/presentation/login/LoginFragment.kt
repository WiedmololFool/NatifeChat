package com.max.natifechat.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.max.natifechat.R
import com.max.natifechat.databinding.FragmentLoginBinding
import com.max.natifechat.log
import com.max.natifechat.presentation.usersList.UsersListFragment

class LoginFragment : BaseLoginFragment() {

    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            progressBar.visibility = View.GONE
            loginButton.setOnClickListener {
                login(username = usernameField.text.toString(), {
                    log("onSuccess")
                    showToast(getString(R.string.connection_success_message))
                    changeFragment(UsersListFragment.newInstance(), false)
                }, {
                    log("onLoading")
                    progressBar.visibility = View.VISIBLE
                }, {
                    progressBar.visibility = View.GONE
                    log("onError")
                    showToast(getString(R.string.connection_failed_message))
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    companion object {

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
package com.max.natifechat.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.max.natifechat.Constants
import com.max.natifechat.databinding.FragmentStartBinding
import com.max.natifechat.log
import com.max.natifechat.presentation.usersList.UsersListFragment


class StartFragment : BaseLoginFragment() {

    private var binding: FragmentStartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStartBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userFromStorage = getUserFromStorage()
        binding?.apply {
            tvConnectionFailed.visibility = View.GONE
            progressBar.visibility = View.GONE
            if (userFromStorage.id == Constants.DEFAULT_USER_ID) {
                log("No login before")
                changeFragment(LoginFragment.newInstance(), false)
            } else {
                login(username = userFromStorage.name, {
                    log("onSuccess")
                    changeFragment(UsersListFragment.newInstance(), false)
                }, {
                    log("onLoading")
                    progressBar.visibility = View.VISIBLE
                }, {
                    log("onError")
                    progressBar.visibility = View.GONE
                    tvConnectionFailed.visibility = View.VISIBLE
                })
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        fun newInstance(): StartFragment {
            return StartFragment()
        }
    }
}
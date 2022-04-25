package com.max.natifechat.presentation.usersList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.natifechat.Constants
import com.max.natifechat.databinding.FragmentUsersListBinding
import com.max.natifechat.presentation.BaseFragment
import com.max.natifechat.presentation.chat.ChatFragment
import com.max.natifechat.presentation.login.StartFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel


class UsersListFragment : BaseFragment() {

    private var binding: FragmentUsersListBinding? = null
    private val viewModel by viewModel<UsersListViewModel>()
    var job: Job? = null
    private val adapter by lazy {
        UsersListAdapter(onItemClickListener = {
            changeFragment(ChatFragment.newInstance(receiverId = it.id), true)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersListBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            rcView.adapter = adapter
            rcView.layoutManager = LinearLayoutManager(requireContext())
            btnLogout.setOnClickListener {
                changeFragment(StartFragment.newInstance(), false)
                job?.cancel()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.logout()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.users.collect { users ->
                adapter.submitList(users)
                UsersHolder.list = users
                Log.e(Constants.TAG, users.toString())
            }
        }
        job = lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    delay(1000)
                    viewModel.loadUsers()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    companion object {

        fun newInstance(): UsersListFragment {
            return UsersListFragment()
        }
    }
}
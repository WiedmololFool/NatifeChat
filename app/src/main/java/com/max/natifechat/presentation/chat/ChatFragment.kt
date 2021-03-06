package com.max.natifechat.presentation.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.max.natifechat.Constants
import com.max.natifechat.databinding.FragmentChatBinding
import com.max.natifechat.presentation.BaseFragment
import com.max.natifechat.presentation.usersList.UsersHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARG_RECEIVER_ID = "receiverId"


class ChatFragment : BaseFragment() {

    private var binding: FragmentChatBinding? = null
    private var receiverId: String? = null
    private val viewModel by viewModel<ChatViewModel> {
        parametersOf(receiverId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            receiverId = it.getString(ARG_RECEIVER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChatBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            val receiver = UsersHolder.getUser(receiverId!!)
            receiverName.text = receiver?.name
            btnSend.setOnClickListener {
                if (inputField.text.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.sendMessage(inputField.text.toString())
                    }
                } else  {
                    showToast("Input some text first")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.messages.onEach { messages ->
                Log.e(Constants.TAG, messages.toString())
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        fun newInstance(receiverId: String): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECEIVER_ID, receiverId)
                }
            }
        }
    }
}
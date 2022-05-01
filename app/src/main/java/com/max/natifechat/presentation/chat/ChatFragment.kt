package com.max.natifechat.presentation.chat

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.natifechat.databinding.FragmentChatBinding
import com.max.natifechat.log
import com.max.natifechat.presentation.BaseFragment
import com.max.natifechat.presentation.usersList.UsersHolder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARG_RECEIVER_ID = "receiverId"


class ChatFragment : BaseFragment() {

    private var binding: FragmentChatBinding? = null
    private var receiverId: String? = null
    private val viewModel by viewModel<ChatViewModel> {
        parametersOf(receiverId)
    }
    private val adapter by lazy {
        ChatAdapter()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            val receiver = UsersHolder.getUser(receiverId!!)
            receiverName.text = receiver?.name
            rcView.adapter = adapter
            rcView.layoutManager = LinearLayoutManager(requireContext())
            rcView.itemAnimator = null
            btnSend.setOnClickListener {
                if (inputField.text.isNotEmpty()) {
                    viewModel.sendMessage(inputField.text.toString())
                    inputField.text.clear()
                    inputField.clearFocus()
                    rcView.scrollToPosition(adapter.itemCount - 1)
                } else {
                    showToast("Input some text first")
                }
            }

        }
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            log(messages.toString())
            adapter.submitList(messages)
        }
        viewModel.loadMessages()

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
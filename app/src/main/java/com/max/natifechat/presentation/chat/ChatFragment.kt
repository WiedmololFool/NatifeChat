package com.max.natifechat.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.natifechat.R
import com.max.natifechat.databinding.FragmentChatBinding
import com.max.natifechat.log
import com.max.natifechat.presentation.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARG_RECEIVER_ID = "receiverId"


class ChatFragment : BaseFragment() {

    private var binding: FragmentChatBinding? = null
    private val receiverId by lazy {
        arguments?.getString(ARG_RECEIVER_ID)
    }
    private val viewModel by viewModel<ChatViewModel> {
        parametersOf(receiverId)
    }
    private val adapter by lazy {
        ChatAdapter()
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
            val receiver = viewModel.loadReceiver()
            receiverName.text = receiver.name
            rcView.adapter = adapter
            rcView.layoutManager = LinearLayoutManager(requireContext())
            rcView.itemAnimator = null
            rcView.scrollToPosition(adapter.itemCount)
            btnSend.setOnClickListener {
                if (inputField.text.isNotEmpty()) {
                    viewModel.sendMessage(inputField.text.toString())
                    inputField.text.clear()
                    inputField.clearFocus()
                } else {
                    showToast(getString(R.string.input_some_text_message))
                }
            }

        }
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            log(messages.toString())
            adapter.submitList(messages) {
                binding?.rcView?.scrollToPosition(adapter.itemCount - 1)
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
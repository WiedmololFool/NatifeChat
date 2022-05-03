package com.max.natifechat.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.max.natifechat.Constants
import com.max.natifechat.databinding.ListReceivedMessageItemBinding
import com.max.natifechat.databinding.ListSendedMessageItemBinding
import com.max.natifechat.data.remote.model.Message

class ChatAdapter : ListAdapter<Message, ChatAdapter.AbstractViewHolder>(MessageComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == Constants.SENDED_MESSAGE) {
            return SendedMessageViewHolder(
                ListSendedMessageItemBinding.inflate(
                    inflater, parent, false
                )
            )
        } else {
            return ReceivedMessageViewHolder(
                ListReceivedMessageItemBinding.inflate(
                    inflater, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        val currentItem = getItem(position)

        if (currentItem != null) {
            holder.bindItem(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.fromMe) Constants.SENDED_MESSAGE else Constants.RECEIVED_MESSAGE
    }


    abstract class AbstractViewHolder(
        binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bindItem(messageItem: Message)
    }


    class SendedMessageViewHolder(
        private val binding: ListSendedMessageItemBinding
    ) : AbstractViewHolder(binding) {

        override fun bindItem(messageItem: Message) {
            binding.apply {
                tvMessage.text = messageItem.message
                tvDate.text = messageItem.date
            }
        }
    }

    class ReceivedMessageViewHolder(
        private val binding: ListReceivedMessageItemBinding
    ) : AbstractViewHolder(binding) {

        override fun bindItem(messageItem: Message) {
            binding.apply {
                tvMessage.text = messageItem.message
                tvDate.text = messageItem.date
            }
        }
    }

    class MessageComparator : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }


}
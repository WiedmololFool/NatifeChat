package com.max.natifechat.presentation.usersList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.max.natifechat.databinding.ListUserItemBinding
import model.User

class UsersListAdapter(
    private val onItemClickListener: (User) -> Unit
) : ListAdapter<User, UsersListAdapter.UserViewHolder>(UserComparator()) {

    inner class UserViewHolder(
        private val binding: ListUserItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bindItem(userItem: User) = with(binding) {
            tvName.text = userItem.name
            root.setOnClickListener {
                onItemClickListener(userItem)
            }
        }
    }

    class UserComparator : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserViewHolder(ListUserItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentItem = getItem(position)

        if (currentItem != null) {
            holder.bindItem(getItem(position))
        }

    }
}
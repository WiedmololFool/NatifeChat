package com.max.natifechat.data.remote.model

data class IsConnected(val status: Boolean) {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return status.hashCode()
    }
}
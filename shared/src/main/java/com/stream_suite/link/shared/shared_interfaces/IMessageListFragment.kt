package com.stream_suite.link.shared.shared_interfaces

interface IMessageListFragment {

    val conversationId: Long

    fun setShouldPullDrafts(pull: Boolean)
    fun loadMessages()
    fun loadMessages(addedNewMessage: Boolean)
    fun setDismissOnStartup()
    fun setConversationUpdateInfo(text: String)
}

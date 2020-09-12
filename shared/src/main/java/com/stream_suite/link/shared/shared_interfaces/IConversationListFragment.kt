package com.stream_suite.link.shared.shared_interfaces

interface IConversationListFragment {

    val isFragmentAdded: Boolean
    val expandedId: Long
    val adapter: IConversationListAdapter?

    fun checkEmptyViewDisplay()
}

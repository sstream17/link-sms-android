package com.stream_suite.link.adapter

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.stream_suite.link.fragment.message.MessageInstanceManager
import com.stream_suite.link.shared.data.model.Conversation

class TvAdapter(val conversations: List<Conversation>) : ArrayObjectAdapter() {

    override fun get(index: Int): Any {
        val conversation = conversations[index]

        val customFragmentAdapter = ArrayObjectAdapter()
        customFragmentAdapter.add(MessageInstanceManager.newInstance(conversation))

        return ListRow(HeaderItem(conversation.title), customFragmentAdapter)
    }
}
package com.stream_suite.link.shared.shared_interfaces

import com.stream_suite.link.shared.data.SectionType
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.pojo.ReorderType

interface IConversationListAdapter {

    val conversations: MutableList<Conversation>
    val sectionCounts: MutableList<SectionType>

    fun findPositionForConversationId(id: Long): Int
    fun getCountForSection(sectionType: Int): Int
    fun removeItem(position: Int, type: ReorderType): Boolean

    fun notifyItemChanged(position: Int)
    fun notifyItemRangeInserted(start: Int, end: Int)
    fun notifyItemInserted(item: Int)
}

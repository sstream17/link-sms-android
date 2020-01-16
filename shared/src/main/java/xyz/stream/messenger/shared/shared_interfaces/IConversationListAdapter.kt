package xyz.stream.messenger.shared.shared_interfaces

import xyz.stream.messenger.shared.data.SectionType
import xyz.stream.messenger.shared.data.model.Conversation
import xyz.stream.messenger.shared.data.pojo.ReorderType

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

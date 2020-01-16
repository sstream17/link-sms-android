package xyz.stream.messenger.adapter.conversation

import android.view.View
import android.widget.TextView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.adapter.view_holder.ConversationViewHolder
import xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper
import xyz.stream.messenger.shared.data.SectionType
import xyz.stream.messenger.shared.data.Settings

class ConversationSectionHeaderBinder(private val adapter: ConversationListAdapter, private val dataProvider: ConversationAdapterDataProvider,
                                      private val activity: MessengerActivity) {

    fun bind(holder: ConversationViewHolder, section: Int) {
        if (holder.header?.visibility != View.VISIBLE) holder.header?.visibility = View.VISIBLE
        if (holder.headerDone?.visibility != View.VISIBLE) holder.headerDone?.visibility = View.VISIBLE
        if (holder.headerCardForTextOnline?.visibility != View.GONE) holder.headerCardForTextOnline?.visibility = View.GONE

        val text = when {
            dataProvider.sectionCounts[section].type == SectionType.PINNED -> holder.itemView.context.getString(R.string.pinned)
            dataProvider.sectionCounts[section].type == SectionType.TODAY -> holder.itemView.context.getString(R.string.today)
            dataProvider.sectionCounts[section].type == SectionType.YESTERDAY -> holder.itemView.context.getString(R.string.yesterday)
            dataProvider.sectionCounts[section].type == SectionType.LAST_WEEK -> holder.itemView.context.getString(R.string.last_week)
            dataProvider.sectionCounts[section].type == SectionType.LAST_MONTH -> holder.itemView.context.getString(R.string.last_month)
            else -> holder.header!!.context.getString(R.string.older)
        }

        holder.header?.text = text

        holder.headerDone?.setOnLongClickListener {
            adapter.swipeToDeleteListener.onShowMarkAsRead(text)
            true
        }

        holder.headerDone?.setOnClickListener {
            if (dataProvider.sectionCounts.size > section) {
                adapter.swipeToDeleteListener.onMarkSectionAsRead(text, dataProvider.sectionCounts[section].type)
            }
        }

        if (!Settings.showConversationCategories) {
            holder.headerContainer?.visibility = View.GONE
        }
    }

    fun bindOnlinePromotion(holder: ConversationViewHolder) {
        holder.header?.visibility = View.GONE
        holder.headerDone?.visibility = View.GONE
        holder.headerCardForTextOnline?.visibility = View.VISIBLE

        val tryIt = holder.headerCardForTextOnline!!.findViewById<View>(R.id.try_it) as TextView
        tryIt.setTextColor(Settings.mainColorSet.color)

        tryIt.setOnClickListener {
            if (dataProvider.sectionCounts.size > 0) {
                dataProvider.sectionCounts.removeAt(0)
            }

            Settings.setValue(activity, activity.getString(R.string.pref_show_text_online_on_conversation_list), false)
            adapter.notifyItemRemoved(0)

            tryIt.postDelayed({
                activity.navController.drawerItemClicked(R.id.drawer_account)
                activity.clickNavigationItem(R.id.drawer_account)
                xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.convoListTryIt(activity)
            }, 500)
        }

        holder.headerCardForTextOnline?.findViewById<View>(R.id.not_now)?.setOnClickListener {
            if (dataProvider.sectionCounts.size > 0) {
                dataProvider.sectionCounts.removeAt(0)
            }

            Settings.setValue(activity, activity.getString(R.string.pref_show_text_online_on_conversation_list), false)
            adapter.notifyItemRemoved(0)
            xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.convoListNotNow(activity)
        }
    }
}
/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import xyz.klinker.messenger.R
import xyz.klinker.messenger.activity.MessengerActivity
import xyz.klinker.messenger.adapter.search.SearchAdapter
import xyz.klinker.messenger.shared.MessengerActivityExtras
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.model.Conversation
import xyz.klinker.messenger.shared.data.model.Message
import xyz.klinker.messenger.shared.util.listener.SearchListener

/**
 * A fragment for searching through conversations and messages.
 */
class SearchFragment : Fragment(), SearchListener {

    private var conversationId: Long? = null
    private var conversationColor: Int? = null
    private var query: String? = null

    var list: RecyclerView? = null
    private val adapter: SearchAdapter by lazy { SearchAdapter(query, null, null, this, conversationColor) }

    private val searchView: MaterialSearchView? by lazy { activity?.findViewById<View>(R.id.search_view) as MaterialSearchView? }

    val isSearching: Boolean
        get() = query != null && query!!.isNotEmpty()

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        list = inflater.inflate(R.layout.fragment_search, parent, false) as RecyclerView

        if (arguments?.containsKey(ARG_CONVERSATION_ID) == true) conversationId = arguments!!.getLong(ARG_CONVERSATION_ID)
        if (arguments?.containsKey(ARG_CONVERSATION_COLOR) == true) conversationColor = arguments!!.getInt(ARG_CONVERSATION_COLOR)

        list?.layoutManager = LinearLayoutManager(activity)
        list?.adapter = adapter

        val messengerActivity = activity
        if (messengerActivity is MessengerActivity) {
            messengerActivity.insetController.modifySearchListElements(this)
        }

        return list
    }

    fun search(query: String?) {
        this.query = query
        loadSearch()
    }

    private fun loadSearch() {
        val handler = Handler()

        Thread {
            val activity = activity

            val conversations = if (activity != null && conversationColor == null) {
                DataSource.searchConversationsAsList(activity, query, 60).toMutableList()
            } else mutableListOf()

            val messages = if (activity != null) {
                if (conversationId == null) DataSource.searchMessagesAsList(activity, query, 60).toMutableList()
                else DataSource.searchConversationMessagesAsList(activity, query, conversationId!!, 60).toMutableList()
            } else mutableListOf()

            handler.post { setSearchResults(conversations, messages) }
        }.start()
    }

    private fun setSearchResults(conversations: MutableList<Conversation>, messages: MutableList<Message>) {
        adapter.updateCursors(query, conversations, messages)
    }

    override fun onSearchSelected(message: Message) {
        val activity = activity ?: return

        DataSource.archiveConversation(activity, message.conversationId, false)

        val intent = Intent(activity, MessengerActivity::class.java)
        intent.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, message.conversationId)
        intent.putExtra(MessengerActivityExtras.EXTRA_MESSAGE_ID, message.id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)

        dismissKeyboard()
    }

    override fun onSearchSelected(conversation: Conversation) {
        val activity = activity ?: return

        if (conversation.archive) {
            DataSource.archiveConversation(activity, conversation.id, false)
        }

        val intent = Intent(activity, MessengerActivity::class.java)
        intent.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, conversation.id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)

        dismissKeyboard()
    }

    private fun dismissKeyboard() {
        searchView?.clearFocus()

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(list?.windowToken, 0)
    }

    companion object {
        private const val ARG_CONVERSATION_ID = "conversation_id"
        private const val ARG_CONVERSATION_COLOR = "conversation_color"

        fun newInstance(conversationId: Long? = null, conversationColor: Int? = null): SearchFragment {
            val fragment = SearchFragment()
            val bundle = Bundle()

            if (conversationId != null) {
                bundle.putLong(ARG_CONVERSATION_ID, conversationId)
            }

            if (conversationColor != null) {
                bundle.putInt(ARG_CONVERSATION_COLOR, conversationColor)
            }

            fragment.arguments = bundle
            return fragment
        }
    }
}

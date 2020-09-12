package com.stream_suite.link.activity.main

import android.view.MenuItem
import android.view.View
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.fragment.SearchFragment
import com.stream_suite.link.shared.util.hide
import com.stream_suite.link.shared.util.show

@Suppress("DEPRECATION")
class MainSearchHelper(private val activity: MessengerActivity) : MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener {
    
    private val navController
        get() = activity.navController
    
    private val searchView: MaterialSearchView by lazy { activity.findViewById<View>(R.id.search_view) as MaterialSearchView }
    private var searchFragment: SearchFragment? = null
    
    fun setup(item: MenuItem) {
        searchView.setVoiceSearch(false)
        searchView.setBackgroundColor(activity.resources.getColor(R.color.background))
        searchView.setOnQueryTextListener(this)
        searchView.setOnSearchViewListener(this)
        
        searchView.setMenuItem(item)
    }
    
    fun closeSearch(): Boolean {
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
            return true
        }
        
        return false
    }
    
    override fun onQueryTextSubmit(query: String): Boolean {
        ensureSearchFragment()
        searchFragment?.search(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotEmpty()) {
            ensureSearchFragment()
            searchFragment!!.search(newText)
        } else {
            searchFragment?.search(null)
        }

        return true
    }

    override fun onSearchViewShown() {
        activity.navController.navigationView.hide()
        ensureSearchFragment()
        displaySearchFragment()
    }

    override fun onSearchViewClosed() {
        ensureSearchFragment()

        if (!searchFragment!!.isSearching) {
            activity.navController.navigationView.show()

            if (navController.conversationListFragment != null && !navController.conversationListFragment!!.isAdded) {
                activity.setTitle(R.string.app_title)
                activity.displayConversations()
            }
        }
    }

    private fun ensureSearchFragment() {
        if (searchFragment == null) {
            searchFragment = SearchFragment.newInstance()
        }
    }

    private fun displaySearchFragment() {
        navController.otherFragment = null

        if (searchFragment != null) {
            try {
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.conversation_list_container, searchFragment!!)
                        .commit()
            } catch (e: Exception) {
            }
        }
    }

}
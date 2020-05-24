package xyz.stream.messenger.activity.main

import android.view.MenuItem
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.fragment.SearchFragment
import xyz.stream.messenger.shared.view.PersistentSearchBarLayout

@Suppress("DEPRECATION")
class MainSearchHelper(private val activity: MessengerActivity) : PersistentSearchBarLayout.OnQueryTextListener, PersistentSearchBarLayout.SearchViewListener {
    
    private val navController
        get() = activity.navController
    
    private val searchView: PersistentSearchBarLayout by lazy { activity.findViewById<View>(R.id.search_view) as PersistentSearchBarLayout }
    private var searchFragment: SearchFragment? = null
    
    fun setup(item: MenuItem) {
        searchView.setBackgroundColor(activity.resources.getColor(R.color.drawerBackground))
    }
    
    fun closeSearch(): Boolean {
        /*if (searchView.isSearchOpen) {
            searchView.closeSearch()
            return true
        }*/
        
        return false
    }
    
    override fun onQueryTextSubmit(query: String): Boolean {
        ensureSearchFragment()
        searchFragment?.search(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotEmpty()) {
            // display search fragment
            ensureSearchFragment()
            searchFragment!!.search(newText)
            if (!searchFragment!!.isAdded) {
                displaySearchFragment()
            }
        } else {
            // display conversation fragment
            ensureSearchFragment()
            searchFragment?.search(null)

            if (navController.conversationListFragment != null && !navController.conversationListFragment!!.isAdded) {
                activity.displayConversations()
                activity.fab.hide()
            }
        }

        return true
    }

    override fun onSearchViewShown() {
        activity.fab.hide()
        ensureSearchFragment()
    }

    override fun onSearchViewClosed() {
        ensureSearchFragment()

        if (!searchFragment!!.isSearching) {
            activity.fab.show()

            if (navController.conversationListFragment != null && !navController.conversationListFragment!!.isAdded) {
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
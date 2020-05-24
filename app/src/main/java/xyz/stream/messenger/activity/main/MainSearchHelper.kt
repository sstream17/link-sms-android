package xyz.stream.messenger.activity.main

import android.view.View
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.fragment.SearchFragment
import xyz.stream.messenger.shared.util.hide
import xyz.stream.messenger.shared.util.show
import xyz.stream.messenger.shared.view.PersistentSearchBarLayout

@Suppress("DEPRECATION")
class MainSearchHelper(private val activity: MessengerActivity) : PersistentSearchBarLayout.OnQueryTextListener, PersistentSearchBarLayout.SearchViewListener {

    private val navController
        get() = activity.navController

    private val searchView: PersistentSearchBarLayout by lazy { activity.findViewById<View>(R.id.search_bar_container) as PersistentSearchBarLayout }
    private var searchFragment: SearchFragment? = null

    fun setup() {
        searchView.setOnQueryTextListener(this)
        searchView.setSearchViewListener(this)
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
        ensureSearchFragment()
        searchFragment!!.search(newText)
        if (!searchFragment!!.isAdded) {
            displaySearchFragment()
        }

        return true
    }

    override fun onSearchOpened() {
        activity.fab.hide()
        activity.bottomNav.hide()
        ensureSearchFragment()
        displaySearchFragment()
    }

    override fun onSearchClosed() {
        ensureSearchFragment()

        if (!searchFragment!!.isSearching) {
            searchFragment!!.dismissKeyboard()
            activity.fab.show()
            activity.bottomNav.show()
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
                        .replace(R.id.conversation_search_list, searchFragment!!)
                        .commit()
            } catch (e: Exception) {
            }
        }
    }

}
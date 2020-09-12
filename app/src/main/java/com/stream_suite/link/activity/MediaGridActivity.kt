package com.stream_suite.link.activity

import android.os.Bundle
import androidx.appcompat.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.stream_suite.link.R
import com.stream_suite.link.activity.main.MainColorController
import com.stream_suite.link.fragment.MediaGridFragment
import com.stream_suite.link.shared.activity.AbstractSettingsActivity
import com.stream_suite.link.shared.util.ActivityUtils
import com.stream_suite.link.shared.util.ColorUtils

class MediaGridActivity : AbstractSettingsActivity() {

    private val fragment: MediaGridFragment by lazy { MediaGridFragment.newInstance(
            intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)) }

    private var mode: ActionMode? = null
    private val actionMode = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater?.inflate(R.menu.action_mode_archive_list, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.removeItem(R.id.menu_archive_conversation)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.menu_delete_conversation -> {
                    fragment.deleteSelected()
                    stopMultiSelect()
                }
            }

            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            fragment.destroyActionMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager.beginTransaction()
                .replace(R.id.settings_content, fragment)
                .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ColorUtils.checkBlackBackground(this)
        MainColorController(this).configureNavigationBarColor()
    }

    public override fun onStart() {
        super.onStart()
        ActivityUtils.setTaskDescription(this, fragment.conversation!!.title!!, fragment.conversation!!.colors.color)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return true
    }

    fun startMultiSelect() {
        mode = startSupportActionMode(actionMode)
    }

    fun stopMultiSelect() {
        mode?.finish()
    }

    companion object {
        val EXTRA_CONVERSATION_ID = "conversation_id"
    }
}

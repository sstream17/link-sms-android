package xyz.stream.messenger.activity.main

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.fragment.bottom_sheet.CustomSnoozeFragment
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.util.TimeUtils

@Suppress("DEPRECATION")
class SnoozeController(private val activity: MessengerActivity) {

    fun initSnooze(optionsMenu: View) {
        val snooze = optionsMenu.findViewById<View>(R.id.snooze) as ImageButton? ?: return

        if (!Settings.isCurrentlyDarkTheme(activity)) {
            snooze.imageTintList = ColorStateList.valueOf(optionsMenu.resources.getColor(R.color.lightToolbarTextColor))
        }

        snooze.setOnClickListener { view ->
            val menu = PopupMenu(activity, view)
            val currentlySnoozed = Settings.snooze > TimeUtils.now
            menu.inflate(if (currentlySnoozed) R.menu.snooze_off else R.menu.snooze)
            menu.setOnMenuItemClickListener { item ->
                val snoozeTil: Long
                when (item.itemId) {
                    R.id.menu_snooze_off -> snoozeTil = TimeUtils.now
                    R.id.menu_snooze_1 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60
                    R.id.menu_snooze_2 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60 * 2
                    R.id.menu_snooze_4 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60 * 4
                    R.id.menu_snooze_8 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60 * 8
                    R.id.menu_snooze_24 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60 * 24
                    R.id.menu_snooze_72 -> snoozeTil = TimeUtils.now + 1000 * 60 * 60 * 72
                    R.id.menu_snooze_custom -> {
                        val fragment = CustomSnoozeFragment()
                        fragment.show(activity.supportFragmentManager, "")
                        snoozeTil = TimeUtils.now
                    }
                // fall through to the default
                    else -> snoozeTil = TimeUtils.now
                }

                Settings.setValue(activity.applicationContext,
                        activity.getString(R.string.pref_snooze), snoozeTil)
                ApiUtils.updateSnooze(Account.accountId, snoozeTil)
                updateSnoozeIcon(optionsMenu)

                true
            }

            menu.show()
        }
    }

    fun updateSnoozeIcon(optionsMenu: View?) {
        val currentlySnoozed = Settings.snooze > TimeUtils.now
        val snooze = optionsMenu?.findViewById<View>(R.id.snooze) as ImageButton?

        if (currentlySnoozed) snooze?.setImageResource(R.drawable.ic_snoozed)
        else snooze?.setImageResource(R.drawable.ic_snooze)
    }

}
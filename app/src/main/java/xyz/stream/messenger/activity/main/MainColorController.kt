package xyz.stream.messenger.activity.main

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.TimeUtils

class MainColorController(private val activity: AppCompatActivity) {

    private val toolbar: Toolbar by lazy { activity.findViewById<View>(R.id.toolbar) as Toolbar }
    private val conversationListContainer: View by lazy { activity.findViewById<View>(R.id.conversation_list_container) }

    fun colorActivity() {
        ColorUtils.checkBlackBackground(activity)
        ActivityUtils.setTaskDescription(activity)

        if (!Build.FINGERPRINT.contains("robolectric")) {
            TimeUtils.setupNightTheme(activity)
        }
    }

    fun configureGlobalColors() {
        if (Settings.isCurrentlyDarkTheme(activity)) {
            activity.window.navigationBarColor = Color.BLACK
        }

        toolbar.setBackgroundColor(Settings.mainColorSet.color)

        ColorUtils.adjustStatusBarColor(Settings.mainColorSet.color, Settings.mainColorSet.colorDark, activity)

        if (Settings.baseTheme == BaseTheme.BLACK) {
            conversationListContainer.setBackgroundColor(Color.BLACK)
        }
    }

    fun configureNavigationBarColor() {
        val isMessengerActivity = activity is MessengerActivity
        when {
            Settings.baseTheme == BaseTheme.BLACK -> ActivityUtils.setUpNavigationBarColor(activity, Color.BLACK, isMessengerActivity)
            Settings.isCurrentlyDarkTheme(activity) -> ActivityUtils.setUpNavigationBarColor(activity, -1223, isMessengerActivity) // random. the activity utils will handle the dark color
            else -> ActivityUtils.setUpNavigationBarColor(activity, Color.WHITE, isMessengerActivity)
        }
    }
}
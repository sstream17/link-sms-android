package xyz.stream.messenger.activity.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.TimeUtils

class MainColorController(private val activity: AppCompatActivity) {

    private val fab: FloatingActionButton by lazy { activity.findViewById<View>(R.id.fab) as FloatingActionButton }
    private val conversationListContainer: View by lazy { activity.findViewById<View>(R.id.conversation_list_container) }
    private val accountColor: CircleImageView by lazy { activity.findViewById<View>(R.id.account_color) as CircleImageView }
    private val defaultIcon: ImageView by lazy { activity.findViewById<View>(R.id.default_icon) as ImageView }

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

        fab.backgroundTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)

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

    fun configureProfilePictureColor(view: View?) {
        val accountColor = if (view != null) view.findViewById(R.id.account_color) as CircleImageView else accountColor
        val defaultIcon = if (view != null) view.findViewById(R.id.default_icon) as ImageView else defaultIcon
        if (Settings.isCurrentlyDarkTheme(activity)) {
            accountColor.setImageDrawable(ColorDrawable(Settings.mainColorSet.colorLight))
            defaultIcon.imageTintList = ColorStateList.valueOf(activity.getColor(R.color.lightToolbarTextColor))
        } else {
            accountColor.setImageDrawable(ColorDrawable(Settings.mainColorSet.colorDark))
            defaultIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
    }
}
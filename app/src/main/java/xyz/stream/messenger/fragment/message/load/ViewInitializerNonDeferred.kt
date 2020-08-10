package xyz.stream.messenger.fragment.message.load

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.activity.MessengerTvActivity
import xyz.stream.messenger.fragment.message.MessageListFragment
import xyz.stream.messenger.shared.data.FeatureFlags
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.data.pojo.BubbleTheme
import xyz.stream.messenger.shared.util.*

@Suppress("DEPRECATION")
class ViewInitializerNonDeferred(private val fragment: MessageListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }

    private val argManager
        get() = fragment.argManager

    val replyBarCard: CardView by lazy { fragment.rootView!!.findViewById<CardView>(R.id.reply_bar_card) }
    private val messageEntry: EditText by lazy { fragment.rootView!!.findViewById<View>(R.id.message_entry) as EditText }
    private val sendProgress: ProgressBar by lazy { fragment.rootView!!.findViewById<View>(R.id.send_progress) as ProgressBar }
    private val send: FloatingActionButton by lazy { fragment.rootView!!.findViewById<View>(R.id.send) as FloatingActionButton }
    private val toolbar: Toolbar by lazy { fragment.rootView!!.findViewById<View>(R.id.toolbar) as Toolbar }
    private val appBarLayout: View by lazy { fragment.rootView!!.findViewById<View>(R.id.app_bar_layout) }
    private val background: View by lazy { fragment.rootView!!.findViewById<View>(R.id.background) }
    private val searchView: MaterialSearchView? by lazy { activity?.findViewById<MaterialSearchView>(R.id.conversation_search_view) }

    fun init(bundle: Bundle?) {
        initToolbar()

        val accent = argManager.colorAccent
        send.backgroundTintList = ColorStateList.valueOf(accent)
        messageEntry.highlightColor = accent

        val firstName = if (argManager.title.isEmpty()) {
            val name = argManager.title.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (name.isNotEmpty()) name[0] else ""
        } else {
            ""
        }

        if (!argManager.isGroup && !firstName.isEmpty()) {
            val hint = fragment.getString(R.string.type_message_to, firstName)
            messageEntry.hint = hint
        } else {
            messageEntry.setHint(R.string.type_message)
        }

        if (Settings.baseTheme == BaseTheme.BLACK) {
            replyBarCard.setCardBackgroundColor(Color.BLACK)
            background.setBackgroundColor(Color.BLACK)
        }

        if (Settings.bubbleTheme != BubbleTheme.ROUNDED) {
            replyBarCard.radius = DensityUtil.toDp(activity, 2).toFloat()
        }

        if (Settings.useGlobalThemeColor) {
            toolbar.setBackgroundColor(Settings.mainColorSet.color)
            send.backgroundTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)
            sendProgress.progressTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)
            sendProgress.progressBackgroundTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)
            messageEntry.highlightColor = Settings.mainColorSet.colorAccent
        }

        if (bundle == null) {
            fragment.messageLoader.initRecycler()
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            // is this causing a crash?
            messageEntry.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        val messengerActivity = fragment.activity
        if (messengerActivity is MessengerActivity) {
            messengerActivity.insetController.modifyMessageListElements(fragment)
        }
    }

    private fun initToolbar() {
        val name = argManager.title
        val color = argManager.color
        val colorAccent = argManager.colorAccent
        val colorDarker = argManager.colorDark

        toolbar.title = name
        toolbar.setBackgroundColor(color)

        if (!fragment.resources.getBoolean(R.bool.pin_drawer)) {
            // phone
            val drawerLayout = activity?.findViewById<View>(R.id.drawer_layout)
            if (drawerLayout is DrawerLayout) {
                drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                    override fun onDrawerClosed(drawerView: View) {}
                    override fun onDrawerStateChanged(newState: Int) {}
                    override fun onDrawerOpened(drawerView: View) {
                        fragment.dismissKeyboard()
                    }
                })
            }

            toolbar.setNavigationIcon(R.drawable.ic_collapse)
            toolbar.setNavigationOnClickListener {
                fragment.dismissKeyboard()
                activity?.onBackPressed()
            }
        } else {
            setNameAndDrawerColor()
        }

        ColorUtils.adjustStatusBarColor(color, colorDarker, activity)

        val deferredTime = if (activity is MessengerTvActivity) 0L
        else (AnimationUtils.EXPAND_CONVERSATION_DURATION + 25).toLong()
        Handler().postDelayed({
            val thisActivity = activity
            if (thisActivity is MessengerActivity) {
                toolbar.inflateMenu(if (argManager.isGroup) R.menu.fragment_messages_group else R.menu.fragment_messages)

                val item = toolbar.menu?.findItem(R.id.menu_search_conversation)
                if (item != null && searchView != null) {
                    fragment.searchHelper.setup(item, searchView!!)
                }
            }

            toolbar.setOnMenuItemClickListener { item ->
                fragment.dismissKeyboard()
                (activity as MessengerActivity).navController.drawerItemClicked(item.itemId)

                false
            }

            if (!fragment.isAdded) {
                return@postDelayed
            }

            if (!fragment.resources.getBoolean(R.bool.pin_drawer)) {
                setNameAndDrawerColor()
            }

            val nav = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
            if (argManager.isArchived) {
                val navItem = nav?.menu?.findItem(R.id.drawer_archive_conversation)
                val toolbarItem = toolbar.menu.findItem(R.id.menu_archive_conversation)

                navItem?.setTitle(R.string.menu_move_to_inbox)
                toolbarItem?.setTitle(R.string.menu_move_to_inbox)
            }

            if (!FeatureFlags.DISPLAY_NOTIFICATION_BUBBLES || !AndroidVersionUtil.isAndroidQ) {
                try {
                    toolbar.menu.findItem(R.id.menu_show_bubble)?.isVisible = false
                } catch (e: Exception) { }
            }

            if (colorAccent == Color.BLACK && Settings.baseTheme == BaseTheme.BLACK) {
                ColorUtils.setCursorDrawableColor(messageEntry, Color.WHITE)
                ColorUtils.colorTextSelectionHandles(messageEntry, Color.WHITE)
            } else {
                ColorUtils.setCursorDrawableColor(messageEntry, colorAccent)
                ColorUtils.colorTextSelectionHandles(messageEntry, colorAccent)
            }
        }, deferredTime)

        if (!TvUtils.hasTouchscreen(activity)) {
            appBarLayout.visibility = View.GONE
        }
    }

    private fun setNameAndDrawerColor() {
        val name = argManager.title
        val phoneNumber = PhoneNumberUtils.format(argManager.phoneNumbers)
        val colorDarker = argManager.colorDark
        val isGroup = argManager.isGroup
        val imageUri = argManager.imageUri

        val nameView = activity?.findViewById<View>(R.id.drawer_header_reveal_name) as TextView?
        val phoneNumberView = activity?.findViewById<View>(R.id.drawer_header_reveal_phone_number) as TextView?
        val image = activity?.findViewById<View>(R.id.drawer_header_reveal_image) as ImageView?

        // could be null when rotating the device
        if (nameView != null) {
            if (name != phoneNumber) {
                nameView.text = name
            } else {
                messageEntry.setHint(R.string.type_message)
                nameView.text = ""
            }

            phoneNumberView?.text = phoneNumber

            image?.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
            if (imageUri != null) {
                Glide.with(fragment)
                        .load(Uri.parse(imageUri))
                        .into(image!!)
            }
        }

        val nav = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
        if (argManager.isArchived) {
            val navItem = nav?.menu?.findItem(R.id.drawer_archive_conversation)
            val toolbarItem = toolbar.menu.findItem(R.id.menu_archive_conversation)

            navItem?.setTitle(R.string.menu_move_to_inbox)
            toolbarItem?.setTitle(R.string.menu_move_to_inbox)
        }

        if (!FeatureFlags.DISPLAY_NOTIFICATION_BUBBLES || !AndroidVersionUtil.isAndroidQ) {
            try {
                toolbar.menu.findItem(R.id.menu_show_bubble)?.isVisible = false
            } catch (e: Exception) { }
        }
    }
}
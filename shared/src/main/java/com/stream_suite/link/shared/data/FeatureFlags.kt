package com.stream_suite.link.shared.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.stream_suite.link.api.implementation.Account

import com.stream_suite.link.shared.R

@Suppress("unused", "MayBeConstant", "MemberVisibilityCanBePrivate")
/**
 * We can use these for new features or if we want to test something quick and don't know if it
 * is going to work. These are great for quick changes. Say we have something that could cause force
 * closes or that we aren't sure users will like. We don't have to go through the play store to be
 * able to change it.
 *
 * These flags should continuously be updated. After we know the flag is no longer necessary,
 * we can remove any code here and any flag implementation to stick with the true (or false) implementation
 * of the flag.
 */
object FeatureFlags {

    private val FLAG_MESSAGING_STYLE_NOTIFICATIONS = "messaging_notifications"
    private val FLAG_ANDROID_WEAR_SECOND_PAGE = "wear_second_page"
    private val FLAG_NO_NOTIFICATION_WHEN_CONVO_OPEN = "hold_notification"
    private val FLAG_REORDER_CONVERSATIONS_WHEN_NEW_MESSAGE_ARRIVES = "reorder_conversations"
    private val FLAG_TURN_DOWN_CONTENT_OBSERVER_TIMEOUT = "content_observer_timeout"
    private val FLAG_REMOVE_MESSAGE_LIST_DRAWER = "remove_message_drawer"
    private val FLAG_ARTICLE_ENHANCER = "flag_media_enhancer"
    private val FLAG_FEATURE_SETTINGS = "flag_feature_settings"
    private val FLAG_SECURE_PRIVATE = "flag_secure_private"
    private val FLAG_QUICK_COMPOSE = "flag_quick_compose"
    private val FLAG_DELAYED_SENDING = "flag_delayed_sending"
    private val FLAG_CLEANUP_OLD = "flag_cleanup_old"
    private val FLAG_NO_GROUP_MESSAGE_COLORS_FOR_GLOBAL = "flag_global_group_colors"
    private val FLAG_BRING_IN_NEW_MESSAGES = "flag_bring_new_messages"
    private val FLAG_BRING_IN_NEW_MESSAGES_2 = "flag_bring_new_messages_3"
    private val FLAG_DELETED_NOTIFICATION_FCM = "flag_fcm_deleted_notification"
    private val FLAG_KEYBOARD_LAYOUT = "flag_keyboard_layout"
    private val FLAG_IMPROVE_MESSAGE_PADDING = "flag_improve_message_list"
    private val FLAG_NOTIFICATION_ACTIONS = "flag_notification_actions"
    private val FLAG_HEADS_UP = "flag_heads_up"
    private val FLAG_MESSAGE_REFRESH_ON_START = "flag_refresh_messages_on_start"
    private val FLAG_NOUGAT_NOTIFICATION_HISTORY = "flag_nougat_notifications"
    private val FLAG_ATTACH_CONTACT = "flag_attach_contact"
    private val FLAG_CONVO_LIST_CARD_ABOUT_TEXT_ANYWHERE = "flag_text_anywhere_convo_list"
    private val FLAG_ONLY_NOTIFY_NEW = "flag_only_notify_new"
    private val FLAG_IMPROVE_CONTACT_MATCH = "flag_improve_contact_match"
    private val FLAG_SNACKBAR_RECEIVED_MESSAGES = "flag_received_message_snackbar"
    private val FLAG_EMOJI_STYLE = "flag_emoji_style"
    private val FLAG_TV_MESSAGE_ENTRY = "flag_tv_message_entry"
    private val FLAG_DATABASE_SYNC_SERVICE_ALL = "flag_database_sync_service_all"
    private val FLAG_REMOVE_IMAGE_BORDERS = "flag_remove_image_borders_beta"
    private val FLAG_WHITE_LINK_TEXT = "flag_white_link_text"
    private val FLAG_AUTO_RETRY_FAILED_MESSAGES = "flag_auto_retry_failed_messages"
    private val FLAG_CHECK_NEW_MESSAGES_WITH_SIGNATURE = "flag_new_messages_with_signature"
    private val FLAG_HEADS_UP_ON_GROUP_PRIORITY = "flag_heads_up_group_priority"
    private val FLAG_RERECEIVE_GROUP_MESSAGE_FROM_SELF = "flag_rereceive_group_message_from_self"
    private val FLAG_BLACK_NAV_BAR = "flag_black_nav_bar"
    private val FLAG_REENABLE_SENDING_STATUS_ON_NON_PRIMARY = "flag_reenable_sending_status"
    private val FLAG_V_2_6_0 = "flag_v_2_6_0"
    private val FLAG_NEVER_SEND_FROM_WATCH = "flag_never_send_from_watch"
    private val FLAG_EMAIL_RECEPTION_CONVERSION = "flag_email_reception_conversion"
    private val FLAG_RECONCILE_RECEIVED_MESSAGES = "flag_reconcile_received_messages"
    private val FLAG_TEMPLATE_SUPPORT = "flag_template_support"
    private val FLAG_FOLDER_SUPPORT = "flag_folder_support"
    private val FLAG_ADJUSTABLE_NAV_BAR = "flag_adjustable_nav_bar"
    private val FLAG_WIDGET_THEMEING = "flag_widget_themeing"
    private val FLAG_MULTI_SELECT_MEDIA = "flag_multi_select_media"
    private val FLAG_SHARE_MULTIPLE_MESSAGES = "flag_share_multiple_messages"
    private val FLAG_AUTO_REPLIES = "flag_auto_replies"
    private val FLAG_VCARD_PREVIEWS = "flag_vcard_previews"
    private val FLAG_DISMISS_NOTI_BY_UNREAD_MESSAGES = "flag_dismiss_noti_by_unread_messages"
    private val FLAG_RETRY_FAILED_REQUESTS = "flag_retry_failed_requests"
    private val FLAG_UPDATE_WEB_MESSAGE_TIMESTAMPS = "flag_update_web_message_timestamps"
    private val FLAG_SEND_SCHEDULED_MESSAGE_IMMEDIATELY = "flag_send_scheduled_immediately"
    private val FLAG_QUERY_DAILY_CONTACT_CHANGES = "flag_query_daily_contact_changes"
    private val FLAG_MARK_AS_UNREAD = "flag_mark_as_unread"
    private val FLAG_CONVO_SNIPPETS_REVAMP = "flag_convo_snippets_revamp"
    private val FLAG_SWIPE_OPTIONS = "flag_swipe_options"
    private val FLAG_DISABLE_HARDWARE_ACCEL = "flag_disable_hardware_accel"
    private val FLAG_OTP_PARSE = "flag_otp_parse"
    private val FLAG_MATERIAL_THEME = "flag_material_theme"
    private val FLAG_TOOLBAR_COLOR = "flag_apply_toolbar_color"
    private val FLAG_CHECK_SUB_STATUS_ON_ACCOUNT_PAGE = "flag_check_sub_status_on_account_page"
    private val FLAG_BLACKLIST_PHRASE = "flag_blacklist_phrase"
    private val FLAG_INTERNAL_CAMERA_REVAMP = "flag_internal_camera_revamp"
    private val FLAG_SCHEDULED_MESSAGE_REVAMP = "flag_scheduled_message_revamp"
    private val FLAG_HEX_COLOR_ENTRY = "flag_hex_color_entry"
    private val FLAG_NOTIFICATION_CHANNEL_CHANGE = "flag_notification_channel_change_2"

    private val ALWAYS_ON_FLAGS = listOf(FLAG_REENABLE_SENDING_STATUS_ON_NON_PRIMARY)


    // ADDING FEATURE FLAGS:
    // 1. Add the static identifiers and flag name right below here.
    // 2. Set up the flag in the constructor
    // 3. Add the switch case for the flag in the updateFlag method

    val SKIP_INTRO_PAGER = true
    val DISPLAY_NOTIFICATION_BUBBLES = false
    val QUICK_SIGN_UP_SYSTEM = Account.QUICK_SIGN_UP_SYSTEM

    // always on
    var REENABLE_SENDING_STATUS_ON_NON_PRIMARY: Boolean = false

    // disabled for future features

    // in testing
    var QUERY_DAILY_CONTACT_CHANGES: Boolean = false

    fun init(context: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        REENABLE_SENDING_STATUS_ON_NON_PRIMARY = getValue(context, sharedPrefs, FLAG_REENABLE_SENDING_STATUS_ON_NON_PRIMARY)

//        QUERY_DAILY_CONTACT_CHANGES = getValue(context, sharedPrefs, FLAG_QUERY_DAILY_CONTACT_CHANGES)
    }

    fun updateFlag(context: Context, identifier: String, flag: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(identifier, flag)
                .apply()

        when (identifier) {
            FLAG_REENABLE_SENDING_STATUS_ON_NON_PRIMARY -> REENABLE_SENDING_STATUS_ON_NON_PRIMARY = flag

            FLAG_QUERY_DAILY_CONTACT_CHANGES -> QUERY_DAILY_CONTACT_CHANGES = flag
        }
    }

    private fun getValue(context: Context, sharedPrefs: SharedPreferences, key: String) =
            context.resources.getBoolean(R.bool.feature_flag_default) || sharedPrefs.getBoolean(key, alwaysOn(key))

    private fun alwaysOn(key: String) = ALWAYS_ON_FLAGS.contains(key)
}

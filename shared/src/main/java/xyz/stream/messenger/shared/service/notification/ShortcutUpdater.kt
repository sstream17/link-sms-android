package xyz.stream.messenger.shared.service.notification

import xyz.stream.messenger.shared.util.TimeUtils

interface ShortcutUpdater {

    fun refreshDynamicShortcuts(delay: Long = 10 * TimeUtils.SECOND)

}
package com.stream_suite.link.shared.service.notification

import com.stream_suite.link.shared.util.TimeUtils

interface ShortcutUpdater {

    fun refreshDynamicShortcuts(delay: Long = 10 * TimeUtils.SECOND)

}
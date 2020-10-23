package com.stream_suite.link.shared.util

import android.database.Cursor

object CursorUtil {

    fun closeSilent(cursor: Cursor?) {
        try {
            cursor?.close()
        } catch (e: Exception) {
        }
    }
}

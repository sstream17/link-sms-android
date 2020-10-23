package com.stream_suite.link.shared.util.listener

import com.stream_suite.link.shared.data.MediaMessage

/**
 * Callback for easily notifying the caller when a media has been selected
 */
interface MediaSelectedListener {
    fun onSelected(messageList: List<MediaMessage>, selectedPosition: Int)
    fun onStartDrag(index: Int)
}

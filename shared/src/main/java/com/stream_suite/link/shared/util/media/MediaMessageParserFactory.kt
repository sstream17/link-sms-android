package com.stream_suite.link.shared.util.media

import android.content.Context
import com.stream_suite.link.shared.data.model.Message

import com.stream_suite.link.shared.util.media.parsers.ArticleParser
import com.stream_suite.link.shared.util.media.parsers.YoutubeParser

/**
 * Create an instance that can be used to parse media from the message text
 */
class MediaMessageParserFactory {

    fun getInstance(context: Context, message: Message): MediaParser? {
        return buildParsers(context).firstOrNull { it.canParse(message) }
    }

    private fun buildParsers(context: Context): Array<MediaParser> {
        return arrayOf(YoutubeParser(context), ArticleParser(context))
    }
}

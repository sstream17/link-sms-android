package com.stream_suite.link.shared.util.vcard

import android.content.Context
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.vcard.parsers.MapLocationVcardParser
import com.stream_suite.link.shared.util.vcard.parsers.TextAttributeVcardParser

class VcardParserFactory {

    fun getInstances(context: Context, message: Message): List<VcardParser> {
        message.data = VcardReader.readCotactCard(context, message.data!!)
        return buildParsers(context).filter { it.canParse(message) }
    }

    private fun buildParsers(context: Context): List<VcardParser> {
        return listOf(MapLocationVcardParser(context), TextAttributeVcardParser(context))
    }

}
package com.stream_suite.link.shared.util.vcard.parsers

import android.content.Context
import com.stream_suite.link.shared.data.MapPreview
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.vcard.VcardParser

class MapLocationVcardParser(context: Context) : VcardParser(context) {

    private var data: String? = null

    override fun canParse(message: Message) = getData(message).isNotEmpty()
    override fun getMimeType(message: Message) = MimeType.MEDIA_MAP

    override fun getData(message: Message): String {
        if (data != null) {
            return data!!
        }

        try {
            val lines = message.data!!.split("\n")
            val readable = getReadableLines(lines)

            val latLong = readable.firstOrNull() ?: return ""
            val mapPreview = MapPreview.build(latLong.split(",")[0], latLong.split(",")[1])
                    ?: return ""
            data = mapPreview.toString()

            return data!!
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun getReadableLines(card: List<String>): List<String> {
        return card.filter { line -> PARSABLE_LINES.firstOrNull { line.contains(it) } != null }
                .map { readLine(it.replace("\r","")) }
                .filter { it.isNotEmpty() }
    }

    private fun readLine(line: String): String {
        return when {
            line.contains(GEO) -> readGeo(line)
            line.contains(APPLE_MAPS) -> readAppleMaps(line)
            else -> throw IllegalArgumentException("this line shouldn't have made it here: $line")
        }
    }

    // GEO:37.386013;-122.082932
    private fun readGeo(line: String): String {
        return line.replace(GEO, "").replace(";", ",")
    }

    // item1.URL;type=pref:http://maps.apple.com/?ll=55.369117\,39.079991
    private fun readAppleMaps(line: String): String {
        if (!line.contains("maps.apple.com")) {
            return ""
        }

        return line.replace("item1.URL;type=pref:http://maps.apple.com/?ll=", "")
                .replace("\\,", ",")
    }

    companion object {
        private const val START_LINE = "BEGIN:VCARD"
        private const val END_LINE = "END:VCARD"

        private const val GEO = "GEO:"
        private const val APPLE_MAPS = "item1.URL;"

        private val PARSABLE_LINES = listOf(GEO, APPLE_MAPS)
    }
}
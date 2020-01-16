package xyz.stream.messenger.shared.data

import org.json.JSONException
import org.json.JSONObject

import xyz.stream.messenger.shared.BuildConfig

class MapPreview {

    var latitude: String? = null
    var longitude: String? = null

    override fun toString(): String {
        val json = serialize()
        return json?.toString() ?: ""
    }

    fun generateMap(): String {
        return "https://maps.googleapis.com/maps/api/staticmap" +
                "?size=600x400" +
//                "&center=$latitude,$longitude" +
//                "&zoom=16" +
//                "&maptype=roadmap" +
                "&markers=color:red%7C$latitude,$longitude" +
                "&key=${xyz.stream.messenger.shared.BuildConfig.YOUTUBE_API_KEY}"
    }

    private fun serialize() = try {
        val json = JSONObject()
        json.put(JSON_LATITUDE, latitude)
        json.put(JSON_LONGITUDE, longitude)

        json
    } catch (e: JSONException) {
        null
    }

    companion object {

        private const val JSON_LATITUDE = "latitude"
        private const val JSON_LONGITUDE = "longitude"

        fun build(latitude: String?, longitude: String?): MapPreview? {
            val preview = MapPreview()

            if (latitude == null || longitude == null) {
                return null
            }

            preview.latitude = latitude
            preview.longitude = longitude

            return preview
        }

        fun build(articleJson: String) = try {
            val json = JSONObject(articleJson)

            val preview = MapPreview()
            preview.latitude = json.getString(JSON_LATITUDE)
            preview.longitude = json.getString(JSON_LONGITUDE)

            preview
        } catch (e: JSONException) {
            null
        }
    }
}

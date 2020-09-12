package com.stream_suite.link.shared.util

import android.content.Context
import android.os.Build
import androidx.core.provider.FontRequest
import android.util.Log
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.stream_suite.link.shared.R
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.pojo.EmojiStyle

object EmojiInitializer {

    fun initializeEmojiCompat(context: Context) {
        val fontRequest = when (Settings.emojiStyle) {
            EmojiStyle.ANDROID_O -> createAndroidODownloadRequest()
            else -> null
        }

        if (fontRequest != null) initializeWithRequest(context, fontRequest)
    }

    private fun createAndroidODownloadRequest(): FontRequest {
        return FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)

    }

    private fun initializeWithRequest(context: Context, fontRequest: FontRequest) {
        EmojiCompat.init(FontRequestEmojiCompatConfig(context, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Log.i("EmojiCompat", "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e("EmojiCompat", "EmojiCompat initialization failed", throwable)
                    }
                }))
    }

    fun isAlreadyUsingGoogleAndroidO(): Boolean {
        return AndroidVersionUtil.isAndroidO && Build.MANUFACTURER.toLowerCase() == "google"
    }
}
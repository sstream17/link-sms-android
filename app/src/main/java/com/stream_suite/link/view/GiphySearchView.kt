package com.stream_suite.link.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import xyz.klinker.giphy.GiphyView
import com.stream_suite.link.BuildConfig
import com.stream_suite.link.R
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.MmsSettings
import com.stream_suite.link.shared.util.listener.ImageSelectedListener

@SuppressLint("ViewConstructor")
class GiphySearchView(context: Context, private val listener: ImageSelectedListener, useStickers: Boolean) : FrameLayout(context) {

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_giphy_search, this, true)

        val toolbarContainer = findViewById<View>(R.id.toolbar_container)
        val searchInput = findViewById<EditText>(R.id.search_view)

        toolbarContainer.setBackgroundColor(context.resources.getColor(R.color.background))
        searchInput.setTextColor(context.resources.getColor(R.color.primaryText))
        searchInput.setHintTextColor(context.resources.getColor(R.color.hintText))

        val giphy = findViewById<GiphyView>(R.id.giphy)
        giphy.setSelectedCallback { uri ->
            listener.onImageSelected(uri, MimeType.IMAGE_GIF)
        }

        giphy.initializeView(BuildConfig.GIPHY_API_KEY, MmsSettings.maxImageSize, useStickers)
    }

}
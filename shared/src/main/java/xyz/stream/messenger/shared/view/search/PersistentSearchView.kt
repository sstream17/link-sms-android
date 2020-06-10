package xyz.stream.messenger.shared.view.search

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.view.emoji.EmojiableEditText

class PersistentSearchView : MaterialCardView {

    internal var currentOffset = 0

    internal val backButtonLayout: FrameLayout by lazy { findViewById<View>(R.id.search_back_holder) as FrameLayout }
    internal val backButton: ImageView by lazy { findViewById<View>(R.id.search_back_button) as ImageView }
    internal val text: EmojiableEditText by lazy { findViewById<View>(R.id.search_text) as EmojiableEditText }
    internal val accountPictureLayout: FrameLayout by lazy { findViewById<View>(R.id.account_image_holder) as FrameLayout }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)
}
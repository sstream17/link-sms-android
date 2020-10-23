package com.stream_suite.link.shared.view.emoji

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.text.InputFilter
import android.util.AttributeSet
import androidx.emoji.widget.EmojiTextViewHelper
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.pojo.EmojiStyle

class EmojiableTextView : AppCompatTextView {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val useEmojiCompat: Boolean
        get() = Settings.emojiStyle != EmojiStyle.DEFAULT

    private var helper: EmojiTextViewHelper? = null
    private val emojiHelper: EmojiTextViewHelper?
        get() {
            if (helper == null) {
                try {
                    helper = EmojiTextViewHelper(this)
                } catch (e: Exception) {
                    return null
                }
            }
            return helper as EmojiTextViewHelper
        }

    init {
        if (useEmojiCompat) {
            try {
                emojiHelper?.updateTransformationMethod()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setFilters(filters: Array<InputFilter>) {
        if (useEmojiCompat && emojiHelper != null) {
            try {
                super.setFilters(emojiHelper!!.getFilters(filters))
            } catch (e: Exception) {
                e.printStackTrace()
                super.setFilters(filters)
            }
        } else {
            super.setFilters(filters)
        }
    }

    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        try {
            emojiHelper?.setAllCaps(allCaps)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
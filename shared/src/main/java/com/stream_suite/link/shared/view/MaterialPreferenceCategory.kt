package com.stream_suite.link.shared.view

import android.content.Context
import android.preference.PreferenceCategory
import android.util.AttributeSet

import com.stream_suite.link.shared.R

class MaterialPreferenceCategory : PreferenceCategory {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    init {
        layoutResource = R.layout.preference_category_card
    }
}

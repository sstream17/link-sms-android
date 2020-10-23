package com.stream_suite.link.fragment.settings

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import android.widget.ListView

import com.stream_suite.link.R
import com.stream_suite.link.shared.util.DensityUtil

/**
 * To be used with the MaterialPreferenceCategory
 */
@Suppress("DEPRECATION")
abstract class MaterialPreferenceFragment : PreferenceFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<View>(android.R.id.list) as ListView
        list.setBackgroundColor(resources.getColor(R.color.background))
        list.divider = ColorDrawable(resources.getColor(R.color.background))
        list.dividerHeight = DensityUtil.toDp(activity, 1)
    }
}

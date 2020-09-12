package com.stream_suite.link.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import com.stream_suite.link.R

@Suppress("DEPRECATION")
abstract class MaterialPreferenceFragmentCompat : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setBackgroundColor(resources.getColor(R.color.background))
    }

    fun findPreference(key: String) = findPreference<Preference>(key as CharSequence)!!
}

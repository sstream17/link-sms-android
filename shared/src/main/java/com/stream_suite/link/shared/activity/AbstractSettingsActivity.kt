package com.stream_suite.link.shared.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View

import com.stream_suite.link.shared.R

open class AbstractSettingsActivity : AppCompatActivity() {

    val toolbar: Toolbar? by lazy { findViewById<View>(R.id.toolbar) as Toolbar? }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
    }
}

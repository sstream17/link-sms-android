package com.stream_suite.link.shared.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.stream_suite.link.shared.MessengerActivityExtras

class RedirectToMyAccount : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)

        val messengerActivity = ActivityUtils.buildForComponent(ActivityUtils.MESSENGER_ACTIVITY)
        messengerActivity.putExtra(MessengerActivityExtras.EXTRA_START_MY_ACCOUNT, true)
        startActivity(messengerActivity)

        overridePendingTransition(0, 0)
        finish()
    }
}

package com.stream_suite.link.shared.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

import com.stream_suite.link.shared.util.ActivityUtils

@RequiresApi(api = Build.VERSION_CODES.N)
class ComposeTileService : TileService() {

    override fun onClick() {
        try {
            val compose = ActivityUtils.buildForComponent(ActivityUtils.QUICK_SHARE_ACTIVITY)
            compose.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(compose)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartListening() {
        try {
            qsTile.state = Tile.STATE_ACTIVE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

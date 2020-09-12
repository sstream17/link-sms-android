package com.stream_suite.link.activity.main

import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.MessengerActivityExtras
import com.stream_suite.link.shared.util.PermissionsUtils

class MainPermissionHelper(private val activity: MessengerActivity) {

    fun requestPermissions() {
        if (PermissionsUtils.checkRequestMainPermissions(activity)) {
            PermissionsUtils.startMainPermissionRequest(activity)
        }
    }

    fun requestDefaultSmsApp() {
        if (Account.primary && !PermissionsUtils.isDefaultSmsApp(activity)) {
            PermissionsUtils.setDefaultSmsApp(activity)
        }
    }

    fun handlePermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            PermissionsUtils.processPermissionRequest(activity, requestCode, permissions, grantResults)
            if (requestCode == MessengerActivityExtras.REQUEST_CALL_PERMISSION) {
                activity.navController.messageActionDelegate.callContact()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
package com.stream_suite.link.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ProgressBar

import com.klinker.android.send_message.Utils

import com.stream_suite.link.R
import com.stream_suite.link.api.implementation.*
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.service.ApiDownloadService
import com.stream_suite.link.shared.service.ApiUploadService
import com.stream_suite.link.shared.util.*
import com.stream_suite.link.shared.util.listener.ProgressUpdateListener

class InitialLoadWearActivity : Activity(), ProgressUpdateListener {

    private val progress: ProgressBar by lazy { findViewById<View>(R.id.loading_progress) as ProgressBar }
    private var handler: Handler? = null
    private var startUploadAfterSync = false
    private var downloadReceiver: BroadcastReceiver? = null

    private val name: String
        get() {
            try {
                val cursor = contentResolver
                        .query(ContactsContract.Profile.CONTENT_URI, null, null, null, null)

                if (cursor != null && cursor.moveToFirst()) {
                    cursor.moveToFirst()
                    val name = cursor.getString(cursor.getColumnIndex("display_name"))
                    CursorUtil.closeSilent(cursor)
                    return name
                } else {
                    CursorUtil.closeSilent(cursor)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }

    private val phoneNumber: String
        get() = try {
            PhoneNumberUtils.clearFormatting(Utils.getMyPhoneNumber(this))
        } catch (e: Exception) {
            ""
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.stream_suite.link.R.layout.activity_initial_load_wear)

        handler = Handler()
        requestPermissions()
    }

    private fun requestPermissions() {
        if (PermissionsUtils.checkRequestMainPermissions(this)) {
            PermissionsUtils.startMainPermissionRequest(this)
        } else {
            startLogin()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        try {
            if (PermissionsUtils.processPermissionRequest(this, requestCode, permissions, grantResults)) {
                startLogin()
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, responseCode: Int, data: Intent?) {
        if (requestCode == SETUP_REQUEST) {
            Settings.forceUpdate(this)

            when (responseCode) {
                Activity.RESULT_CANCELED -> {
                    val account = Account
                    account.setDeviceId(this, null)
                    account.setPrimary(this, false)

                    startDatabaseSync()
                }
                com.stream_suite.link.api.implementation.LoginActivity.RESULT_START_DEVICE_SYNC -> {
                    startDatabaseSync()
                    startUploadAfterSync = true
                }
                com.stream_suite.link.api.implementation.LoginActivity.RESULT_START_NETWORK_SYNC -> {
                    ApiDownloadService.start(this)
                    downloadReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            close()
                        }
                    }

                    registerReceiver(downloadReceiver,
                            IntentFilter(ApiDownloadService.ACTION_DOWNLOAD_FINISHED))
                }
                com.stream_suite.link.api.implementation.ActivateActivity.RESULT_FAILED -> finish()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver)
            downloadReceiver = null
        }
    }

    private fun startLogin() {
        // we want to pass the extras from the last intent to this one, since they will tell us if
        // we should automatically skip the login and just go into the data load.
        val login = Intent(this, ActivateWearActivity::class.java)
        login.putExtras(intent)

        startActivityForResult(login, SETUP_REQUEST)
    }

    private fun startDatabaseSync() {
        Thread {
            val context = applicationContext
            val startTime = TimeUtils.now

            val myName = name
            val myPhoneNumber = PhoneNumberUtils.format(phoneNumber)

            val account = Account
            account.setName(this@InitialLoadWearActivity, myName)
            account.setPhoneNumber(this@InitialLoadWearActivity, myPhoneNumber)

            val source = DataSource
            val conversations = SmsMmsUtils.queryConversations(context)
            source.insertConversations(conversations, context, this@InitialLoadWearActivity)

            handler!!.post { progress.isIndeterminate = true }

            val contacts = ContactUtils.queryContacts(context, source)
            source.insertContacts(context, contacts, null)

            handler!!.postDelayed({ close() }, 5000)

            Log.v("initial_load", "load took " +
                    (TimeUtils.now - startTime) + " ms")
        }.start()
    }

    private fun close() {
        Settings.setValue(this, getString(R.string.pref_first_start), false)

        startActivity(Intent(this, MessengerActivity::class.java))

        if (startUploadAfterSync) {
            ApiUploadService.start(this)
        }

        finish()
    }

    override fun onProgressUpdate(current: Int, max: Int) {
        handler!!.post {
            progress.isIndeterminate = false
            progress.max = max

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progress.setProgress(current, true)
            } else {
                progress.progress = current
            }
        }
    }

    override fun onBackPressed() {
        // don't let them back out of this
    }

    companion object {
        private val SETUP_REQUEST = 54321
    }

}

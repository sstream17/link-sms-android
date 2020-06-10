package xyz.stream.messenger.fragment.bottom_sheet

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.util.TimeUtils

class CustomSnoozeFragment : TabletOptimizedBottomSheetDialogFragment() {

    private var minutes: EditText? = null
    private var hours: EditText? = null

    private val minutesTime: Long
        get() = readNumberSafely(minutes) * TimeUtils.MINUTE

    private val hoursTime: Long
        get() = readNumberSafely(hours) * TimeUtils.HOUR

    override fun createLayout(inflater: LayoutInflater): View {
        val contentView = inflater.inflate(R.layout.bottom_sheet_custom_snooze, null, false)

        minutes = contentView.findViewById<View>(R.id.minutes) as EditText
        hours = contentView.findViewById<View>(R.id.hours) as EditText
        contentView.findViewById<View>(R.id.snooze).setOnClickListener { snooze() }

        hours!!.setSelection(hours!!.text.length)

        return contentView
    }

    private fun snooze() {
        val snoozeTil = TimeUtils.now + minutesTime + hoursTime
        val activity = activity ?: return

        Settings.setValue(activity, getString(R.string.pref_snooze), snoozeTil)
        ApiUtils.updateSnooze(Account.accountId, snoozeTil)

        dismiss()

        if (activity is MessengerActivity) {
            activity.snoozeController.updateSnoozeIcon(activity.navController.optionsMenuLayout)
        }
    }

    private fun readNumberSafely(et: EditText?): Int {
        return try {
            Integer.parseInt(et!!.text.toString())
        } catch (e: Exception) {
            0
        }
    }
}

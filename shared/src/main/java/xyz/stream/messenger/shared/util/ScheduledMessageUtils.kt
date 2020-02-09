package xyz.stream.messenger.shared.util

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.text.format.DateFormat
import android.util.Log
import android.widget.TextView
import java.util.*

object ScheduledMessageUtils {

    // samsung messed up the date picker in some languages on Lollipop 5.0 and 5.1. Ugh.
    // fixes this issue: http://stackoverflow.com/a/34853067
    private fun getContextToFixDatePickerCrash(activity: Activity): ContextWrapper {
        return object : ContextWrapper(activity!!) {
            private var wrappedResources: Resources? = null
            override fun getResources(): Resources {
                val r = super.getResources()
                if (wrappedResources == null) {
                    wrappedResources = object : Resources(r.assets, r.displayMetrics, r.configuration) {
                        @Throws(Resources.NotFoundException::class)
                        override fun getString(id: Int, vararg formatArgs: Any): String {
                            return try {
                                super.getString(id, *formatArgs)
                            } catch (ifce: IllegalFormatConversionException) {
                                Log.e("DatePickerDialogFix", "IllegalFormatConversionException Fixed!", ifce)
                                var template = super.getString(id)
                                template = template.replace(("%" + ifce.conversion).toRegex(), "%s")
                                String.format(configuration.locale, template, *formatArgs)
                            }

                        }
                    }
                }

                return wrappedResources!!
            }
        }
    }

    fun displayDateDialog(fragmentActivity: Activity, scheduledMessageDate: Calendar, date: TextView, time: TextView) {
        var context: Context? = getContextToFixDatePickerCrash(fragmentActivity)

        if (context == null) {
            context = fragmentActivity
        }

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val originalDate = scheduledMessageDate.clone() as Calendar
            TimeUtils.zeroCalendarDay(originalDate)
            val calendarDate = GregorianCalendar(year, month, day).timeInMillis
            val offset = scheduledMessageDate.timeInMillis - originalDate.timeInMillis
            scheduledMessageDate.timeInMillis = calendarDate + offset
            updateTimeInputs(fragmentActivity, scheduledMessageDate, date, time)
        }

        val datePickerDialog = DatePickerDialog(
                context,
                listener,
                scheduledMessageDate.get(Calendar.YEAR),
                scheduledMessageDate.get(Calendar.MONTH),
                scheduledMessageDate.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = TimeUtils.now
        datePickerDialog.show()
    }

    fun displayTimeDialog(fragmentActivity: Activity, scheduledMessageDate: Calendar, date: TextView, time: TextView) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(fragmentActivity, { _, hourOfDay, minute ->
            TimeUtils.zeroCalendarDay(scheduledMessageDate)
            scheduledMessageDate.timeInMillis = scheduledMessageDate.timeInMillis + 1000 * 60 * 60 * hourOfDay
            scheduledMessageDate.timeInMillis = scheduledMessageDate.timeInMillis + 1000 * 60 * minute

            if (scheduledMessageDate.timeInMillis < TimeUtils.now) {
                scheduledMessageDate.timeInMillis = TimeUtils.now
            }

            updateTimeInputs(fragmentActivity, scheduledMessageDate, date, time)
        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(fragmentActivity))
                .show()
    }

    private fun updateTimeInputs(fragmentActivity: Activity, newDate: Calendar, date: TextView, time: TextView){
        date.text = DateFormat.format("MMM dd, yyyy", newDate)
        val timeFormat = if (DateFormat.is24HourFormat(fragmentActivity)) "HH:mm" else "hh:mm a"
        time.text = if (newDate.timeInMillis < TimeUtils.now + 10 * TimeUtils.SECOND) {
            "Now"
        } else {
            DateFormat.format(timeFormat, newDate)
        }
    }
}

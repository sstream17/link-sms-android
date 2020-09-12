package com.stream_suite.link.shared.util

import android.content.Context
import android.util.TypedValue

object DensityUtil {

    fun toPx(context: Context?, dp: Int): Int {
        return convert(context, dp, TypedValue.COMPLEX_UNIT_PX)
    }

    fun toDp(context: Context?, px: Int): Int {
        return convert(context, px, TypedValue.COMPLEX_UNIT_DIP)
    }

    fun spToPx(context: Context?, sp: Int): Int {
        if (context == null) {
            return 0
        }

        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return (sp * scaledDensity).toInt()
    }

    private fun convert(context: Context?, amount: Int, conversionUnit: Int): Int {
        if (amount < 0) {
            throw IllegalArgumentException("px should not be less than zero")
        }

        if (context == null) {
            return 0
        }

        val r = context.resources
        return TypedValue.applyDimension(conversionUnit, amount.toFloat(), r.displayMetrics).toInt()
    }
}
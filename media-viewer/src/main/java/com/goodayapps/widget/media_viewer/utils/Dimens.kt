package com.goodayapps.widget.media_viewer.utils

import android.content.Context
import android.util.DisplayMetrics

internal fun Context.dp(dp: Float): Int {
    val metrics = resources.displayMetrics
    return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

internal fun Context.dp(dp: Int): Int = dp(dp.toFloat())
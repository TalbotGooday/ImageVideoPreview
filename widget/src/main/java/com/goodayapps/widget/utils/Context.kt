package com.goodayapps.widget.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Context.colorAttribute(@AttrRes colorRes: Int): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = theme
    theme.resolveAttribute(colorRes, typedValue, true)
    return typedValue.data
}

fun Context.setStatusBarColor(@ColorInt color: Int) {
    val activity = this as? AppCompatActivity ?: if (this is Fragment) this.activity else null
    activity ?: return

    val view = activity.window?.decorView
    view ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity.window?.statusBarColor = color
    }
}

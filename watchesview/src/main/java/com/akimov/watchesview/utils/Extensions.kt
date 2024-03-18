package com.akimov.watchesview.utils

import android.content.Context
import android.util.TypedValue

internal fun Int.dpToPx(context: Context): Float = (this * context.resources.displayMetrics.density)

internal fun Int.pxToDp(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this / density).toInt()
}

internal inline fun <reified T> MutableList<T>.circularShift(shift: Int) {
    val n = this.size
    val temp = Array<T>(n) { this[0] }
    for (i in this.indices) {
        temp[(i + shift) % n] = this[i]
    }
    for (i in this.indices) {
        this[i] = temp[i]
    }
}
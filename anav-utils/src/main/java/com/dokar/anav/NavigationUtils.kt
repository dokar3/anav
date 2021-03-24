@file:JvmName("NavUtils")

package com.dokar.anav

import android.content.Context
import android.content.Intent

fun Context.navigate(className: String) {
    navigate(className) {}
}

inline fun Context.navigate(className: String, block: (Intent) -> Unit) {
    Intent(Intent.ACTION_VIEW).let {
        it.setClassName(this, className)
        block(it)
        startActivity(it)
    }
}

fun Context.navigationIntent(className: String): Intent {
    return Intent(Intent.ACTION_VIEW).also {
        it.setClassName(this, className)
    }
}

fun navigationBuilder(context: Context, className: String): Navigation.Builder {
    return Navigation.Builder(context, className)
}

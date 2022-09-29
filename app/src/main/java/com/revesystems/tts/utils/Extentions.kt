package com.revesystems.tts.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.FileUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.widget.TextView
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun TextView.changeColor(forText: String, foregroundColor: Int? = null, style: StyleSpan? = null) {
    val spannable: Spannable = SpannableString(text)

    // check if the text we're highlighting is empty to abort
    if (forText.isEmpty()) {
        return
    }

    // compute the start and end indices from the text
    val startIdx = text.indexOf(forText)
    val endIdx = startIdx + forText.length

    // if the indices are out of bounds, abort as well
    if (startIdx < 0 || endIdx > text.length) {
        return
    }

    // check if we can apply the foreground color
    foregroundColor?.let {
        spannable.setSpan(
            ForegroundColorSpan(it),
            startIdx,
            endIdx,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }

    // check if we have a style span
    style?.let {
        spannable.setSpan(
            style,
            startIdx,
            endIdx,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }

    // apply it
    text = spannable
}
package com.revesystems.tts.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.Settings
import android.provider.Settings.Global.putLong
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun hasPermissions10(context: Context, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

@RequiresApi(Build.VERSION_CODES.R)
fun hasPermission11():Boolean{
    return Environment.isExternalStorageManager()
}

@RequiresApi(Build.VERSION_CODES.R)
fun Context.requestStoragePermission11():Intent{
    return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        .addCategory("android.intent.category.DEFAULT")
        .setData(Uri.parse(String.format("package:%s",this.packageName)))
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

fun Long.toByteArray() = numberToByteArray(Long.SIZE_BYTES) { putLong(this@toByteArray) }

private inline fun numberToByteArray(size: Int, bufferFun: ByteBuffer.() -> ByteBuffer): ByteArray =
    ByteBuffer.allocate(size).bufferFun().array()

fun shortToByteArray(s: Short): ByteArray {
    return byteArrayOf((s.toInt() and 0x00FF).toByte(), ((s.toInt() and 0xFF00) shr (8)).toByte())
}

fun numberToByteArray (data: Number, size: Int = 4) : ByteArray =
    ByteArray (size) {i -> (data.toLong() shr (i*8)).toByte()}

fun intToBytes(i: Int): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(i).array()

fun byteArrayToNumber(bytes: ByteArray?, numOfBytes: Int, type: Int): ByteBuffer {
    val buffer: ByteBuffer = ByteBuffer.allocate(numOfBytes)
    if (type == 0) {
        buffer.order(ByteOrder.BIG_ENDIAN)
    } else {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }
    buffer.put(bytes)
    buffer.rewind()
    return buffer
}
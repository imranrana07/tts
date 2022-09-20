package com.revesystems.tts.utils

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Patterns
import androidx.core.app.ActivityCompat
import com.revesystems.tts.core.ApplicationClass
import com.revesystems.tts.data.source.local.datastore.DataStore

fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

//fun Context.dataStore() = DataStore(this)

//val appContext = ApplicationClass.instance?.applicationContext
package com.revesystems.tts.data.source.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.revesystems.tts.utils.USER_DATA
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DataStore/*(private val context: Context)*/ {

    private val Context.dataStore by preferencesDataStore(USER_DATA)
//    val dataStore = appContext.createDataStore(name = "sample")

    suspend fun saveString(context: Context,key: String, value: String) {
        context.dataStore.edit { pref ->
            pref[stringPreferencesKey(key)] = value
        }
    }
    suspend fun getStringVal(context: Context,key: String): String {
        val pre = context.dataStore.data.first()
        return pre[stringPreferencesKey(key)] ?: "Text"
    }

    fun getStringValF(context: Context,key: String):Flow<String?> = context.dataStore.data.map {
        it[stringPreferencesKey(key)] ?: "Flow"
    }

}
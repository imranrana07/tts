package com.revesystems.tts.data.source.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.revesystems.tts.core.ApplicationClass.Companion.dataStore
import com.revesystems.tts.utils.USER_DATA
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(USER_DATA)

    suspend fun saveString(key: String, value: String) {
        context.dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }
    fun getStringVal(key: String): Flow<String?> = context.dataStore.data.map {
        it[stringPreferencesKey(key)]
    }

}
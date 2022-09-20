package com.revesystems.tts.core

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.revesystems.tts.data.source.local.sharedpreference.SharedPreferenceModelImpl

class ApplicationClass:Application() {

    val appContext = applicationContext
    override fun onCreate() {
        super.onCreate()
    }

    companion object{
        val appContext = ApplicationClass::class.java
        var instance: ApplicationClass? = null
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        val sharedPreference: SharedPreferenceModelImpl
            get() {
                return SharedPreferenceModelImpl(instance?.applicationContext!!)
            }
    }

}
package com.revesystems.tts.core

import android.app.Application
import com.revesystems.tts.data.source.local.sharedpreference.SharedPreferenceModelImpl

class ApplicationClass:Application() {
    init {
        instance = this
    }
    override fun onCreate() {
        super.onCreate()
    }

    companion object{
        private var instance: ApplicationClass? = null

        val sharedPreference: SharedPreferenceModelImpl
            get() {
                return SharedPreferenceModelImpl(instance?.applicationContext!!)
            }
    }

}
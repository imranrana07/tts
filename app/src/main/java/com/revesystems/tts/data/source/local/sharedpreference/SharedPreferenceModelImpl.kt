package com.revesystems.tts.data.source.local.sharedpreference

import android.content.Context
import com.revesystems.tts.utils.USER_DATA

class SharedPreferenceModelImpl(context: Context): SharedPreferenceModel {
    private val sharedPreference = context.getSharedPreferences(USER_DATA,Context.MODE_PRIVATE)
    private val editor = sharedPreference.edit()

    override fun saveString(key: String, value: String?) {
        editor.putString(key,value).apply()
    }

    override fun getString(key: String): String? {
        return sharedPreference.getString(key,null)
    }

    override fun saveInt(key: String, value: Int) {
        editor.putInt(key,value).apply()
    }

    override fun getInt(key: String): Int {
        return sharedPreference.getInt(key,-1)
    }

    override fun saveFloat(key: String, value: Float) {
        editor.putFloat(key,value).apply()
    }

    override fun getFloat(key: String): Float {
        return sharedPreference.getFloat(key,-1f)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        editor.putBoolean(key,value).apply()
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreference.getBoolean(key,false)
    }

    override fun setLong(key: String, value: Long) {
        editor.putLong(key,value).apply()
    }

    override fun getLong(key: String): Long {
        return sharedPreference.getLong(key,-1)
    }

    override fun clearData() {
        editor.clear().apply()
    }
}
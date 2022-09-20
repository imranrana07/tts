package com.revesystems.tts.data.source.local.sharedpreference

interface SharedPreferenceModel {
    fun saveString(key: String,value: String?)
    fun getString(key: String): String?
    fun saveInt(key: String,value: Int)
    fun getInt(key: String): Int
    fun saveFloat(key: String,value: Float)
    fun getFloat(key: String): Float
    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean
    fun setLong(key : String, value : Long)
    fun getLong(key : String) : Long
    fun clearData()
}
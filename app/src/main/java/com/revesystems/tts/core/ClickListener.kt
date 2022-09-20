package com.revesystems.tts.core

interface ClickListener<T> {
    fun clickedData(data: T)
}
package com.revesystems.tts.data.model

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class ResModel(
    @SerializedName("duration")
    var duration: String?,
    @SerializedName("output")
    var output: String?,
    @SerializedName("phone")
    var phone: String?,
    @SerializedName("text")
    var text: String?
)
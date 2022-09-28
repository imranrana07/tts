package com.revesystems.tts.data.api

import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.data.model.ResModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

const val SYNTHESIZER = "synthesizer"
interface ApiCall {
    @POST(SYNTHESIZER)
    suspend fun getAudio(
        @Body text: ReqModel
    ):Response<ResModel>
}
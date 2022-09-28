package com.revesystems.tts.data.source.remote.repo

import com.revesystems.tts.data.api.ApiResponse
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.data.model.ResModel
import com.revesystems.tts.utils.apiCall

class HomeRepo {
    suspend fun getAudio(reqModel: ReqModel): ResModel {
        return ApiResponse.getResult { apiCall?.getAudio(reqModel) }
    }
}
package com.revesystems.tts.data.api

import com.revesystems.tts.utils.ApiException
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

object ApiResponse {
    suspend fun <T: Any> getResult(call: suspend() -> Response<T>?): T{
        val response = try {
            call.invoke()
        }catch (e: IOException){
            throw ApiException(e.localizedMessage!!)
        }

        if (response?.isSuccessful == true){
            return response.body()!!
        }else{
            var errorTitle: String? = null
            var errorMessage: String? = null
            val error = response?.errorBody()!!.string()
            if (response.code()!= 500){
            error.let{
                    try {
                        val errors = JSONObject(error)
                        if (errors.keys().hasNext()) {
                            errorTitle = errors.keys().next()
                            errorMessage = if (errors is JSONArray) {
                                errors.getJSONArray(errorTitle!!)[0].toString()
                            } else {
                                errors.optString(errorTitle)
                            }
                        }
                    }catch (e:Exception){
                        throw ApiException("কিছু ভুল হচ্ছে")
                    }
                }
                throw ApiException(errorMessage.toString())
            }
            throw ApiException("কিছু ভুল হচ্ছে")
        }
    }
}
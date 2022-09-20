package com.revesystems.tts.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.revesystems.tts.data.api.ApiCall
import com.revesystems.tts.data.api.RetrofitClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

const val BASE_URL = "http://dev.revesoft.com"
//const val BASE_URL = "http://dev.revesoft.com/crowd-app-test"
//const val BASE_URL = "http://192.168.26.102:8080"

val interceptor: Interceptor = Interceptor { chain ->

    var request: Request = chain.request()
    request = request.newBuilder()
//            .addHeader("User-Agent", "wad4")
        .build()

    chain.proceed(request)
}

val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
class ApiException(message: String): IOException(message)

fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
    }
    return result
}
// api call initialization
val apiCall = RetrofitClient.retrofit?.create(ApiCall::class.java)
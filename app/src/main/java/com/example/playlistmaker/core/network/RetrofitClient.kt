package com.example.playlistmaker.core.network

import com.example.playlistmaker.core.api.service.ITunesApiService
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(
    private val gson: Gson,
    private val okHttpClient: OkHttpClient
) {

    companion object {
        private const val BASE_URL = "https://itunes.apple.com/"
    }

    fun createApiService(): ITunesApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ITunesApiService::class.java)
    }
}

// Функции для создания зависимостей
fun createGson(): Gson {
    return com.google.gson.GsonBuilder().create()
}

fun createOkHttpClient(): OkHttpClient {
    val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
        level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
}
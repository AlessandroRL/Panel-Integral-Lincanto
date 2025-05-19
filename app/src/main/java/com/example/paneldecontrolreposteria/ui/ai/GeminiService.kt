package com.example.paneldecontrolreposteria.ui.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    internal const val MODEL = "models/gemini-2.0-flash"
    private const val API_KEY = "AIzaSyAYbWWTdcXnCXIO--WeJjptmDcUMbAkpFM"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("key", API_KEY)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)
}

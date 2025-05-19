package com.example.paneldecontrolreposteria.ui.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("${GeminiService.MODEL}:generateContent")
    suspend fun generarRespuesta(
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
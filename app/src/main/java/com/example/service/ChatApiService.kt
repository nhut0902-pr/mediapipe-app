package com.example.service

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val message: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val response: String
)

interface ChatApiService {
    @POST
    suspend fun sendChat(
        @Url url: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

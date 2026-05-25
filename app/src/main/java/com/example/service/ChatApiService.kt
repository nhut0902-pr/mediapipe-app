package com.example.service

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
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

@JsonClass(generateAdapter = true)
data class NvidiaChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class NvidiaChatRequest(
    val model: String,
    val messages: List<NvidiaChatMessage>,
    val temperature: Double = 0.5,
    val top_p: Double = 1.0,
    val max_tokens: Int = 1024,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NvidiaChoice(
    val index: Int?,
    val message: NvidiaChatMessage?,
    val finish_reason: String?
)

@JsonClass(generateAdapter = true)
data class NvidiaChatResponse(
    val id: String?,
    val choices: List<NvidiaChoice>?
)

interface ChatApiService {
    @POST
    suspend fun sendChat(
        @Url url: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @POST
    suspend fun sendNvidiaChat(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: NvidiaChatRequest
    ): Response<NvidiaChatResponse>
}


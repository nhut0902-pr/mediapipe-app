package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class UpdateRepository {

    private val baseRetrofitUrl = "https://raw.githubusercontent.com/"

    // Free, unlimited raw GitHub URL checked by default
    private val defaultGitRawPath = "nhut0902-pr/mediapipe-app/main/version.json"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val updateApi: UpdateApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseRetrofitUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UpdateApi::class.java)
    }

    suspend fun checkUpdate(rawPath: String? = null): Result<UpdateInfo> {
        return try {
            val path = rawPath ?: defaultGitRawPath
            val updateInfo = updateApi.checkUpdate(path)
            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val forceUpdate: Boolean,
    val changelog: String
)

interface UpdateApi {
    @GET
    suspend fun checkUpdate(@Url url: String): UpdateInfo
}

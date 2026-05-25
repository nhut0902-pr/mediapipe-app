package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun MultiPlatformDownloaderView(onBack: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var detectedPlatform by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val client = remember { OkHttpClient() }
    
    fun getPlatform(link: String): String {
        return when {
            link.contains("tiktok.com") -> "TikTok"
            link.contains("facebook.com") || link.contains("fb.watch") -> "Facebook"
            link.contains("instagram.com") -> "Instagram"
            link.contains("twitter.com") || link.contains("x.com") -> "Twitter/X"
            link.contains("youtube.com") || link.contains("youtu.be") -> "YouTube"
            link.contains("bilibili.com") || link.contains("b23.tv") -> "Bilibili"
            link.contains("reddit.com") -> "Reddit"
            link.contains("pinterest.com") || link.contains("pin.it") -> "Pinterest"
            link.contains("twitch.tv") -> "Twitch"
            link.contains("vimeo.com") -> "Vimeo"
            link.contains("snapchat.com") -> "Snapchat"
            link.contains("tumblr.com") -> "Tumblr"
            link.contains("soundcloud.com") -> "SoundCloud"
            link.contains("dailymotion.com") -> "Dailymotion"
            link.contains("vk.com") || link.contains("vkvideo.ru") -> "VK"
            link.contains("loom.com") -> "Loom"
            link.contains("ok.ru") -> "OK.ru"
            link.contains("rutube.ru") -> "Rutube"
            link.contains("streamable.com") -> "Streamable"
            link.contains("bluesky") || link.contains("bsky.app") -> "Bluesky"
            link.contains("xiaohongshu.com") -> "Xiaohongshu"
            link.isNotBlank() -> "Không xác định"
            else -> ""
        }
    }
    
    LaunchedEffect(url) {
        detectedPlatform = getPlatform(url)
    }

    fun downloadVideo(videoUrl: String, fileName: String = "video_${System.currentTimeMillis()}.mp4") {
        try {
            val request = DownloadManager.Request(Uri.parse(videoUrl))
            request.setTitle("Đang tải tệp")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            
            val mimeType = if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                "image/jpeg"
            } else {
                "video/mp4"
            }
            request.setMimeType(mimeType)
            
            @Suppress("DEPRECATION")
            request.allowScanningByMediaScanner()
            
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "Bắt đầu tải xuống", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF12131C))
        .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại", tint = Color.White)
            }
            Text("Tải video, ảnh đa nền tảng (beta)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Dán link video/ảnh", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFF0050), unfocusedBorderColor = Color.LightGray)
        )
        
        if (detectedPlatform.isNotEmpty()) {
            Text(
                text = "Nền tảng: $detectedPlatform",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (url.contains("http")) {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                val json = """{"url":"$url"}"""
                                val request = Request.Builder()
                                    .url("https://cobalt-10-yf7k.onrender.com/")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Content-Type", "application/json")
                                    .post(json.toRequestBody("application/json".toMediaType()))
                                    .build()
                                client.newCall(request).execute()
                            }
                            
                            val resultString = response.body?.string()
                            
                            if (response.isSuccessful && !resultString.isNullOrEmpty()) {
                                try {
                                    val jsonObj = JSONObject(resultString)
                                    val status = jsonObj.optString("status")
                                    
                                    when (status) {
                                        "stream", "redirect" -> {
                                            val downloadUrl = jsonObj.getString("url")
                                            downloadVideo(downloadUrl)
                                        }
                                        "picker" -> {
                                            val pickerArray = jsonObj.getJSONArray("picker")
                                            Toast.makeText(context, "Tìm thấy ${pickerArray.length()} tệp. Đang tải...", Toast.LENGTH_SHORT).show()
                                            for (i in 0 until pickerArray.length()) {
                                                val item = pickerArray.getJSONObject(i)
                                                val type = item.optString("type", "file")
                                                val ext = if (type == "photo") "jpg" else "mp4"
                                                val downloadUrl = item.getString("url")
                                                downloadVideo(downloadUrl, "media_${System.currentTimeMillis()}_$i.$ext")
                                            }
                                        }
                                        "error" -> {
                                            val text = jsonObj.optString("text", "Lỗi không xác định")
                                            Toast.makeText(context, "Lỗi: $text", Toast.LENGTH_LONG).show()
                                        }
                                        else -> {
                                            if (jsonObj.has("url")) {
                                                val downloadUrl = jsonObj.getString("url")
                                                downloadVideo(downloadUrl)
                                            } else {
                                                Toast.makeText(context, "Không thể xử lý phản hồi: $status", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi phân tích: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val errorObj = try { JSONObject(resultString ?: "") } catch (e: Exception) { null }
                                val errorMsg = errorObj?.optString("text", "Lỗi API: ${response.code}") ?: "Lỗi API: ${response.code}"
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Link không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0050)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Tải xuống", fontWeight = FontWeight.Bold)
            }
        }
    }
}

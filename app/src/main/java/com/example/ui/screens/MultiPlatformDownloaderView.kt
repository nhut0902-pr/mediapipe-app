package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
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
    var mediaItems by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
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
        if (url.isBlank()) mediaItems = emptyList()
    }

    fun downloadVideo(videoUrl: String, fileName: String = "video_${System.currentTimeMillis()}.mp4") {
        try {
            val request = DownloadManager.Request(Uri.parse(videoUrl))
            request.setTitle("Đang tải tệp")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            
            val mimeType = if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                "image/jpeg"
            } else {
                "video/mp4"
            }
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            request.addRequestHeader("Accept", "*/*")
            
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = Color.White)
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
                                if (detectedPlatform == "TikTok") {
                                    val requestBody = okhttp3.FormBody.Builder()
                                        .add("url", url)
                                        .build()
                                    val request = Request.Builder()
                                        .url("https://www.tikwm.com/api/")
                                        .post(requestBody)
                                        .build()
                                    client.newCall(request).execute()
                                } else {
                                    val json = JSONObject().apply {
                                        put("url", url)
                                    }.toString()
                                    val request = Request.Builder()
                                        .url("https://cobalt-10-yf7k.onrender.com/")
                                        .header("Accept", "application/json")
                                        .post(json.toRequestBody("application/json".toMediaType()))
                                        .build()
                                    client.newCall(request).execute()
                                }
                            }
                            
                            val resultString = response.body?.string()
                            
                            if (response.isSuccessful && !resultString.isNullOrEmpty()) {
                                try {
                                    val jsonObj = JSONObject(resultString)
                                    
                                    if (jsonObj.has("code") && jsonObj.optInt("code", -1) == 0) {
                                        // TikWM API response
                                        val data = jsonObj.getJSONObject("data")
                                        val items = mutableListOf<Map<String, String>>()
                                        
                                        if (data.has("images")) {
                                            val images = data.getJSONArray("images")
                                            for (i in 0 until images.length()) {
                                                items.add(mapOf("url" to images.getString(i), "type" to "photo"))
                                            }
                                        } else if (data.has("play")) {
                                            items.add(mapOf("url" to data.getString("play"), "type" to "video"))
                                        }
                                        
                                        if (items.isNotEmpty()) {
                                            mediaItems = items
                                        } else {
                                            Toast.makeText(context, "Không tìm thấy media", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // Cobalt API response
                                        val status = jsonObj.optString("status")
                                        
                                        when (status) {
                                            "stream", "redirect" -> {
                                                val downloadUrl = jsonObj.getString("url")
                                                mediaItems = listOf(mapOf("url" to downloadUrl, "type" to "video"))
                                            }
                                            "picker" -> {
                                                val pickerArray = jsonObj.getJSONArray("picker")
                                                val items = mutableListOf<Map<String, String>>()
                                                for (i in 0 until pickerArray.length()) {
                                                    val item = pickerArray.getJSONObject(i)
                                                    items.add(mapOf(
                                                        "url" to item.getString("url"),
                                                        "type" to item.optString("type", "photo")
                                                    ))
                                                }
                                                mediaItems = items
                                            }
                                            "error" -> {
                                                val errorObj = jsonObj.optJSONObject("error")
                                                val text = errorObj?.optString("code") ?: jsonObj.optString("text", "Lỗi không xác định")
                                                Toast.makeText(context, "Lỗi API Server: $text", Toast.LENGTH_LONG).show()
                                            }
                                            else -> {
                                                if (jsonObj.has("url")) {
                                                    val downloadUrl = jsonObj.getString("url")
                                                    mediaItems = listOf(mapOf("url" to downloadUrl, "type" to "video"))
                                                } else if (jsonObj.has("msg")) {
                                                     Toast.makeText(context, "Lỗi: ${jsonObj.getString("msg")}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Không thể xử lý phản hồi: $status", Toast.LENGTH_SHORT).show()
                                                }
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
                Text(if (mediaItems.isEmpty()) "Kiểm tra link" else "Tải lại", fontWeight = FontWeight.Bold)
            }
        }
        
        if (mediaItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kết quả (${mediaItems.size} tệp):", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mediaItems.size) { index ->
                    val item = mediaItems[index]
                    val type = item["type"] ?: "video"
                    val itemUrl = item["url"] ?: ""
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F2A))
                    ) {
                        Row(
                           modifier = Modifier.padding(8.dp),
                           verticalAlignment = Alignment.CenterVertically
                        ) {
                           if (type == "photo") {
                                AsyncImage(
                                    model = itemUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                           } else {
                                val imageLoader = ImageLoader.Builder(context)
                                    .components {
                                        add(VideoFrameDecoder.Factory())
                                    }
                                    .crossfade(true)
                                    .build()
                                    
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(itemUrl)
                                            .build(),
                                        imageLoader = imageLoader,
                                        contentDescription = "Video Thumbnail",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.align(Alignment.Center).size(30.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(15.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                           }
                           
                           Spacer(modifier = Modifier.width(16.dp))
                           
                           Column(modifier = Modifier.weight(1f)) {
                               Text(if (type == "photo") "Hình ảnh" else "Video", color = Color.White, fontWeight = FontWeight.Bold)
                           }
                           
                           IconButton(onClick = {
                               val ext = if (type == "photo") "jpg" else "mp4"
                               downloadVideo(itemUrl, "media_${System.currentTimeMillis()}_$index.$ext")
                           }) {
                               Icon(Icons.Filled.Download, contentDescription = "Tải", tint = Color(0xFFFF0050))
                           }
                        }
                    }
                }
            }
        }
    }
}

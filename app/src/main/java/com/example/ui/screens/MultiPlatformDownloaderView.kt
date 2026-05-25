package com.example.ui.screens

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
import android.widget.Toast
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val client = remember { OkHttpClient() }
    
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (url.contains("http")) {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                val json = JSONObject().apply {
                                    put("url", url)
                                    put("vQA", true)
                                }
                                val request = Request.Builder()
                                    .url("https://cobalt-10-yf7k.onrender.com/")
                                    .addHeader("Accept", "application/json")
                                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                                    .build()
                                client.newCall(request).execute()
                            }
                            
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Đã gửi link tới Cobalt API!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Lỗi API: ${response.code}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
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

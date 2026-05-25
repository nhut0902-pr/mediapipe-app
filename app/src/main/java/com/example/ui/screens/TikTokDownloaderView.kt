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

@Composable
fun TikTokDownloaderView(onBack: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
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
            Text("Tải video TikTok", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Dán link video TikTok", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFF0050), unfocusedBorderColor = Color.LightGray)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (url.contains("tiktok.com")) {
                    isLoading = true
                    Toast.makeText(context, "Đang xử lý tách watermark...", Toast.LENGTH_SHORT).show()
                    // Simulate processing
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

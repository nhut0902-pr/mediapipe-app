package com.example

import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.io.BufferedReader

class ApiTest {
    @Test
    fun testCobaltApi() {
        val url = URL("https://www.tikwm.com/api/")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.doOutput = true
        
        val inputString = "url=https://www.tiktok.com/@tiktok/video/7106594312292453675"
        
        try {
            OutputStreamWriter(conn.outputStream).use { os ->
                os.write(inputString)
                os.flush()
            }
            
            val code = conn.responseCode
            println("--- COBALT RESPONSE CODE ---: ${code}")
            
            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            println("--- COBALT RESPONSE BODY ---:\n${response.take(500)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

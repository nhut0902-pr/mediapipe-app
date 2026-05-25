import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.io.BufferedReader

fun main() {
    val url = URL("https://cobalt-10-yf7k.onrender.com/")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Accept", "application/json")
    conn.setRequestProperty("Content-Type", "application/json")
    conn.doOutput = true
    
    val jsonInputString = "{\"url\":\"https://vt.tiktok.com/ZSxHSTfaA/\"}"
    
    try {
        OutputStreamWriter(conn.outputStream).use { os ->
            os.write(jsonInputString)
            os.flush()
        }
        
        val code = conn.responseCode
        println("Response Code: ${code}")
        
        val `is` = if (code >= 400) conn.errorStream else conn.inputStream
        val response = BufferedReader(InputStreamReader(`is`)).use { it.readText() }
        println("Response Body:\n$response")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

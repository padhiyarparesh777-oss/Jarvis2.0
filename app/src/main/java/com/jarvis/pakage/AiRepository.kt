package com.jarvis.pakage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val text: String, val fromUser: Boolean)

object AiRepository {
    suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            try { return@withContext askGemini(prompt) }
            catch (error: Exception) { errors += "Gemini: " + (error.message ?: "request failed") }
        }
        if (BuildConfig.GROQ_API_KEY.isNotBlank()) {
            try { return@withContext askGroq(prompt) }
            catch (error: Exception) { errors += "Groq: " + (error.message ?: "request failed") }
        }
        if (errors.isNotEmpty()) throw IllegalStateException(errors.joinToString(" | "))
        throw IllegalStateException("API key missing. Add GEMINI_API_KEY or GROQ_API_KEY to local.properties, then rebuild.")
    }
    private fun askGemini(prompt: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + BuildConfig.GEMINI_API_KEY
        val body = JSONObject().put("contents", JSONArray().put(
            JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        ))
        val response = postJson(endpoint, body)
        val candidates = response.optJSONArray("candidates")
            ?: throw IllegalStateException(response.optJSONObject("error")?.optString("message") ?: "No Gemini response")
        return candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            .getJSONObject(0).getString("text").trim()
    }
    private fun askGroq(prompt: String): String {
        val body = JSONObject().put("model", "llama-3.3-70b-versatile")
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .put("temperature", 0.7)
        val response = postJson("https://api.groq.com/openai/v1/chat/completions", body,
            "Bearer " + BuildConfig.GROQ_API_KEY)
        return response.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
            .getString("content").trim()
    }
    private fun postJson(endpoint: String, body: JSONObject, authorization: String? = null): JSONObject {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20000
            connection.readTimeout = 45000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            if (authorization != null) connection.setRequestProperty("Authorization", authorization)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = if (raw.isBlank()) JSONObject() else JSONObject(raw)
            if (status !in 200..299) throw IllegalStateException(
                json.optJSONObject("error")?.optString("message") ?: "HTTP " + status
            )
            return json
        } finally { connection.disconnect() }
    }
}

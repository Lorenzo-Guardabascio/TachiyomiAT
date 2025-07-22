package eu.kanade.translation.translator

import eu.kanade.translation.model.PageTranslation
import eu.kanade.translation.recognizer.TextRecognizerLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import logcat.logcat

class PerplexityTranslator(
    override val fromLang: TextRecognizerLanguage,
    override val toLang: TextTranslatorLanguage,
    private val apiKey: String,
    private val modelName: String,
    private val maxOutputTokens: Int,
    private val temp: Float,
) : TextTranslator {

    override suspend fun translate(pages: MutableMap<String, PageTranslation>) = withContext(Dispatchers.IO) {
        try {
            val data = pages.mapValues { (_, v) -> v.blocks.map { b -> b.text } }
            val promptStructure = """
                You are a professional manga/comic translator. Translate each text item to ${toLang.label}. 
                Preserve the JSON structure. Replace any watermarks or site links (e.g., 'colamanga.com') with 'RTMTH'.
                Output must be a valid JSON with identical keys and number of elements as the input.
            """.trimIndent()
            val contentJson = JSONObject(data).toString()

            val requestBody = JSONObject(mapOf(
                "model" to modelName,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to promptStructure),
                    mapOf("role" to "user", "content" to contentJson)
                ),
                "max_tokens" to maxOutputTokens,
                "temperature" to temp
            )).toString()

            val url = URL("https://api.perplexity.ai/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.outputStream.use { os ->
                OutputStreamWriter(os).use { it.write(requestBody) }
            }
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val choices = JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            val resJson = JSONObject(choices)

            for ((k, v) in pages) {
                v.blocks.forEachIndexed { i, b ->
                    val res = resJson.optJSONArray(k)?.optString(i, "NULL")
                    b.translation = if (res == null || res == "NULL") b.text else res
                }
                v.blocks = v.blocks.filterNot { it.translation.contains("RTMTH") }.toMutableList()
            }
        } catch (e: Exception) {
            logcat { "Perplexity Translation Error: ${e.stackTraceToString()}" }
            throw e
        }
    }

    override fun close() {}
}

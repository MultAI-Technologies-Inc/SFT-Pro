import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.charset.StandardCharsets

@Serializable
data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
data class OllamaResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean
)

object OllamaApi {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private const val OLLAMA_URL = "http://localhost:11434/api/generate"

    suspend fun generateJsonl(text: String, model: String): String {
        val prompt = """
You are an expert in preparing data for Supervised Fine-Tuning (SFT).
Your task is to convert the following document text into a JSONL format.
Each line in the output must be a valid JSON object.
Extract the most important information from the document and represent it as key-value pairs in the JSON objects.
The goal is to create a dataset that could be used to fine-tune a model to understand and extract information from similar documents.

Here is the document text:
---
$text
---

Produce the JSONL output now.
        """.trimIndent()

        val request = OllamaRequest(
            model = model,
            prompt = prompt
        )

        try {
            val response: HttpResponse = client.post(OLLAMA_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val ollamaResponse = Json.decodeFromString<OllamaResponse>(responseBody)
                return ollamaResponse.response
            } else {
                return "Error: ${response.status} - ${response.bodyAsText()}"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return "Error connecting to Ollama: ${e.message}. Make sure Ollama is running."
        }
    }
}

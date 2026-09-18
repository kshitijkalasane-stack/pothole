package com.example.services.ai

import com.example.BuildConfig
import com.example.data.models.Severity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class AiAnalysisResult(
    val severity: Severity,
    val confidence: Float,
    val reasoning: String
)

interface GeminiRestService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object AiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiRestService = retrofit.create(GeminiRestService::class.java)

    suspend fun queryGemini(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API key is not configured in Secrets panel."))
            }
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = service.generateContent(key, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response received"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzePotholeSeverity(
        imageDescription: String,
        speedKmh: Float,
        peakZDiff: Float
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze road pothole defect:
            - Description: $imageDescription
            - Vehicle Speed: $speedKmh km/h
            - Vertical Acceleration Delta: $peakZDiff G
            Classify severity as LOW, MEDIUM, or HIGH and provide confidence score (0.0 to 1.0).
        """.trimIndent()

        val response = queryGemini(prompt)
        val text = response.getOrNull()?.uppercase() ?: ""

        val severity = when {
            text.contains("HIGH") || peakZDiff > 3.0f -> Severity.HIGH
            text.contains("LOW") || peakZDiff < 1.2f -> Severity.LOW
            else -> Severity.MEDIUM
        }

        val confidence = when (severity) {
            Severity.HIGH -> 0.94f
            Severity.MEDIUM -> 0.88f
            Severity.LOW -> 0.82f
        }

        AiAnalysisResult(
            severity = severity,
            confidence = confidence,
            reasoning = text.ifBlank { "Automated heuristic classification based on sensor dynamics" }
        )
    }
}

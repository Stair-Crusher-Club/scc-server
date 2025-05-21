package club.staircrusher.place.infra.adapter.out.web

//import club.staircrusher.place.application.port.`in`.accessibility.image.ImageInspectionService
//import club.staircrusher.place.domain.model.check.ImageInspectionResult
//import com.google.genai.Client
//import com.google.genai.types.Content
//import com.google.genai.types.Part
//import com.openai.client.okhttp.OkHttpClient
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.withContext
//import java.io.IOException
//import java.util.concurrent.TimeUnit // For OkHttpClient timeouts
//
//internal class GeminiImageInspectionService(
//    private val properties: GoogleAiProperties
//) : ImageInspectionService {
//
//    private val generativeClient: Client = Client.builder().apiKey(properties.apiKey).build()
//    private val httpClient = OkHttpClient.Builder()
//        .connectTimeout(10, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS) // Images can take a while to download
//        .build()
//
//    override suspend fun inspect(imageUrls: List<String>): List<ImageInspectionService.Result> =
//        withContext(Dispatchers.IO) { // Network operations should be on IO dispatcher
//            imageUrls.map { url ->
//                async { // Process each URL concurrently
//                    var inspectionResult: ImageInspectionResult = ImageInspectionResult.NotVisible
//                    var fetchedBytes: ByteArray? = null
//
//                    try {
//                        // 1. Fetch image data from the URL
//                        fetchedBytes = fetchImageBytes(url)
//
//                        if (fetchedBytes == null) {
//                            println("Failed to fetch image from $url. Skipping inspection.")
//                            return@async ImageInspectionService.Result(url, ImageInspectionResult.NotVisible)
//                        }
//
//                        // 2. Create ImagePart from fetched bytes
//                        // You need to correctly infer or specify the MIME type.
//                        // For simplicity, we'll use a common image type, but a real app
//                        // might parse the URL extension or HTTP Content-Type header.
//                        val mimeType = getMimeTypeFromUrl(url)
//                        val imagePart = Part.fromImageData(mimeType, fetchedBytes)
//
//                        // 3. Crafting the prompt to extract specific information
//                        // It's crucial to ask for structured output or specific keywords.
//                        val prompt = """
//                            Analyze this image.
//                            1. Is there anything clearly visible and identifiable? If not, respond with "NOT_VISIBLE".
//                            2. If visible, identify if there is an 'Elevator', 'Entrance', or 'Stair'. List all detected objects.
//                            3. Estimate the rotation of the main subject or the image content itself relative to an upright view. Provide one of these: D0, D90, D180, D270.
//
//                            Provide your response in a concise format. Example:
//                            STATUS:VISIBLE;OBJECTS:Elevator,Stair;ROTATION:D90
//                            STATUS:NOT_VISIBLE;ROTATION:D0
//                        """.trimIndent()
//
//                        val content = Content.fromParts(
//                            Part.fromText(prompt),
//                            imagePart
//                        )
//
//                        // The Java SDK returns a CompletableFuture, so we use await()
//                        val generateContentResponse = generativeClient.models
//                            .generateContent("gemini-pro-vision", content, null)
//                            .get() // Blocking call, but inside a suspend function on Dispatchers.IO
//
//                        val textResponse = generateContentResponse.text() ?: "STATUS:NOT_VISIBLE;ROTATION:D0"
//                        println("Gemini response for $url: $textResponse") // For debugging
//
//                        inspectionResult = parseGeminiResponse(textResponse)
//
//                    } catch (e: Exception) {
//                        println("Error inspecting image $url: ${e.message}")
//                        // Default to ImageInspectionResult.NotVisible on error
//                        inspectionResult = ImageInspectionResult.NotVisible
//                    }
//                    ImageInspectionService.Result(url, inspectionResult)
//                }
//            }.awaitAll() // Wait for all concurrent inspections to complete
//        }
//
//    /**
//     * Fetches image bytes from a given URL.
//     * Returns null if fetching fails.
//     */
//    private fun fetchImageBytes(imageUrl: String): ByteArray? {
//        return try {
//            val request = Request.Builder().url(imageUrl).build()
//            httpClient.newCall(request).execute().use { response ->
//                if (!response.isSuccessful) {
//                    println("Failed to fetch image from $imageUrl: ${response.code} ${response.message}")
//                    return null
//                }
//                response.body?.bytes()
//            }
//        } catch (e: IOException) {
//            println("Network error fetching $imageUrl: ${e.message}")
//            null
//        } catch (e: Exception) {
//            println("Unexpected error fetching $imageUrl: ${e.message}")
//            null
//        }
//    }
//
//    /**
//     * Tries to infer the MIME type from the URL's file extension.
//     * This is a simple helper; for a production system, you might want a more robust MIME type detection.
//     */
//    private fun getMimeTypeFromUrl(url: String): String {
//        return when {
//            url.endsWith(".png", ignoreCase = true) -> "image/png"
//            url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
//            url.endsWith(".gif", ignoreCase = true) -> "image/gif"
//            url.endsWith(".webp", ignoreCase = true) -> "image/webp"
//            else -> "image/jpeg" // Default or throw an error if unknown
//        }
//    }
//
//    /**
//     * Parses the Gemini text response into our ImageInspectionResult sealed class.
//     * This parsing is crucial and needs to be robust against variations in Gemini's output.
//     */
//    private fun parseGeminiResponse(responseText: String): ImageInspectionResult {
//        val upperCaseResponse = responseText.uppercase()
//
//        // Extract status first
//        val statusMatch = "STATUS:([A-Z_]+)".toRegex().find(upperCaseResponse)
//        val status = statusMatch?.groupValues?.get(1)
//
//        // Extract rotation
//        val rotationMatch = "ROTATION:(D[0-9]{1,3}|UNKNOWN)".toRegex().find(upperCaseResponse)
//        val rotation = rotationMatch?.groupValues?.get(1)?.let { rotationString ->
//            try {
//                Rotation.valueOf(rotationString.trim())
//            } catch (e: IllegalArgumentException) {
//                Rotation.UNKNOWN
//            }
//        } ?: Rotation.UNKNOWN // Default to UNKNOWN if not found or malformed
//
//        if (status == "NOT_VISIBLE") {
//            return ImageInspectionResult.NotVisible
//        }
//
//        // Parse for Visible details if status is not NOT_VISIBLE
//        val objectsMatch = "OBJECTS:([A-Z_,]+)".toRegex().find(upperCaseResponse)
//        val detectedObjects = objectsMatch?.groupValues?.get(1)
//            ?.split(",")
//            ?.mapNotNull { objString ->
//                try {
//                    DetectedObject.valueOf(objString.trim())
//                } catch (e: IllegalArgumentException) {
//                    DetectedObject.UNKNOWN // Handle unknown objects gracefully
//                }
//            }?.filter { it != DetectedObject.UNKNOWN } ?: emptyList() // Filter out UNKNOWN if desired
//
//        return Visible(objects = detectedObjects, rotation = rotation)
//    }
//}

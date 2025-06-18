package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageInspectionService
import club.staircrusher.place.domain.model.check.ImageInspectionResult
import club.staircrusher.place.infra.adapter.out.image_process.ClassicImageInspectionService
import club.staircrusher.stdlib.di.annotation.Component
import com.openai.client.okhttp.OpenAIOkHttpClientAsync
import com.openai.models.ChatModel
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseInputImage
import com.openai.models.responses.ResponseInputItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.*
import kotlin.time.ExperimentalTime


@Component
internal class OpenAIImageInspectionService(
    private val properties: OpenAIProperties,
    private val classicImageInspectionService: ClassicImageInspectionService,
) : ImageInspectionService {
    private val openai = OpenAIOkHttpClientAsync
        .builder()
        .apiKey(properties.apiKey!!)
        .build()

    private val rateLimit = Semaphore(5) // 5 concurrent calls

    override suspend fun inspect(imageUrls: List<String>): List<ImageInspectionService.Result> {
        return processImageUrlsInBatches(imageUrls)
    }

    private suspend fun processImageUrlsInBatches(
        urls: List<String>,
        batchSize: Int = 3,
    ): List<ImageInspectionService.Result> {
        return coroutineScope {
            urls.chunked(batchSize).map { batch ->
                async(Dispatchers.IO) {
                    val urlsWithImage = batch.mapNotNull {
                        val image = downloadTempImage(it)
                        if (image == null) null else it to image
                    }
                    val goodImages = withContext(Dispatchers.Default) {
                        urlsWithImage.filter { (url, image) -> classicImageInspectionService.isImageQualityGood(image) }
                    }
                    val gptResponse = safeAskAboutImages(goodImages.map { it.first })
                    urlsWithImage.forEach { (_, image) -> image.delete() } // Clean up temp

                    // Process the response for each URL
                    batch.map { url ->
                        val isGood = goodImages.map { it.first }.contains(url)
                        val responseForUrl = gptResponse.find { it.first == url }
                        if (responseForUrl == null || !isGood) {
                            return@map ImageInspectionService.Result(
                                url = url,
                                detectionResult = ImageInspectionResult.NotVisible,
                            )
                        }
                        val (rotationResponse, objectResponse) = responseForUrl.second.split("\n")
                        val rotation = when (rotationResponse) {
                            "0" -> ImageInspectionResult.Rotation.D0
                            "90" -> ImageInspectionResult.Rotation.D90
                            "180" -> ImageInspectionResult.Rotation.D180
                            "270" -> ImageInspectionResult.Rotation.D270
                            else -> ImageInspectionResult.Rotation.D0
                        }
                        val containingObjects = objectResponse.split(",").mapNotNull {
                            when (it) {
                                "elevator" -> ImageInspectionResult.DetectedObject.Elevator
                                "entrance" -> ImageInspectionResult.DetectedObject.Entrance
                                "stair" -> ImageInspectionResult.DetectedObject.Stair
                                else -> null
                            }
                        }
                        ImageInspectionService.Result(
                            url = url,
                            detectionResult = ImageInspectionResult.Visible(
                                objects = containingObjects,
                                rotation = rotation
                            ),
                        )
                    }
                }
            }.flatMap { it.await() }
        }
    }

    private suspend fun safeAskAboutImages(imageUrls: List<String>): List<Pair<String, String>> {
        return withRetryAndRateLimit {
            imageUrls.zip(askAboutImages(imageUrls))
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun <T> withRetryAndRateLimit(
        maxRetries: Int = 3,
        block: suspend () -> T
    ): T {
        rateLimit.acquire()
        try {
            var currentDelay = 1L
            repeat(maxRetries - 1) {
                try {
                    return block()
                } catch (e: Exception) {
                    println("⚠️ OpenAI call failed: ${e.message}, retrying in ${currentDelay}s...")
                    delay(currentDelay * 1000)
                    currentDelay *= 2
                }
            }
            return block() // Final attempt
        } finally {
            rateLimit.release()
        }
    }

    private suspend fun askAboutImages(imageUrls: List<String>): List<String> = coroutineScope {
        val images = imageUrls.map {
            ResponseInputImage.builder()
                .detail(ResponseInputImage.Detail.AUTO)
                .imageUrl(it)
                .build()
        }
        val messageInputItem = ResponseInputItem.ofMessage(
            ResponseInputItem.Message.builder()
                .role(ResponseInputItem.Message.Role.USER)
                .addInputTextContent(
                    "You are given a set of images. For each image, respond in exactly two lines:\n" +
                        "\n" +
                        "First line: the image's rotation angle, chosen from only one of these values: 0, 90, 180, or 270.\n" +
                        "Second line: a comma-separated list of the objects present in the image. Only use: entrance, stair, elevator. If none are present, leave the line blank.\n" +
                        "Analyze each image carefully and respond in this exact format for each image."
                )
                .also { images.forEach { img -> it.addContent(img) } }
                .build()
        )
        val createParams = ResponseCreateParams.builder()
            .inputOfResponse(listOf(messageInputItem))
            .model(ChatModel.GPT_4_1)
            .maxOutputTokens(500)
            .build()

        val response = openai.responses().create(createParams)
            .await()
            .output().stream()
            .flatMap { it.message().stream() }
            .flatMap { it.content().stream() }
            .flatMap { it.outputText().stream() }
            .map { it.text() }
            .toList()
        return@coroutineScope response
    }

    private fun downloadTempImage(url: String): File? = try {
        val file = File.createTempFile("img_${UUID.randomUUID()}", ".jpg")
        URL(url).openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        println("❌ Download failed: ${e.message}")
        null
    }
}

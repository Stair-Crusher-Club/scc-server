package club.staircrusher.accesssibility.infra.adapter.`in`.controller.base

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.out.DetectFacesResponse
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.domain.model.DetectedFacePosition
import club.staircrusher.stdlib.Size
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.springframework.boot.test.mock.mockito.MockBean

class BlurFacesITBase : SccSpringITBase() {

    @MockBean
    lateinit var imageProcessor: ImageProcessor

    @MockBean
    lateinit var detectFacesService: DetectFacesService


    fun mockDetectFacesWithFaceImage(imageUrl: String, imageBytes: ByteArray) = runBlocking {
        Mockito.`when`(detectFacesService.detect(eq(imageUrl))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes, imageSize = Size(100, 100), positions = listOf(DetectedFacePosition(0, 0, 10, 10))
            )
        )
    }

    fun mockDetectFacesWithNoFaceImage(imageUrl: String, imageBytes: ByteArray) = runBlocking {
        Mockito.`when`(detectFacesService.detect(eq(imageUrl))).thenReturn(
            DetectFacesResponse(
                imageBytes = imageBytes, imageSize = Size(100, 100), positions = emptyList()
            )
        )
    }
}

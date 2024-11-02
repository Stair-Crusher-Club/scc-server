package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.place.application.port.`in`.CreateClosedPlaceCandidatesUseCase
import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.OpenDataService
import club.staircrusher.place.application.result.ClosedPlaceResult
import club.staircrusher.place.infra.adapter.`in`.controller.base.PlaceITBase
import club.staircrusher.stdlib.geography.Location
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDate
import java.util.UUID

class CreateClosedPlaceCandidateTest : PlaceITBase() {
    @MockBean
    lateinit var openDataService: OpenDataService

    @Autowired
    lateinit var createClosedPlaceCandidatesUseCase: CreateClosedPlaceCandidatesUseCase

    @Autowired
    lateinit var closedPlaceCandidateRepository: ClosedPlaceCandidateRepository

    @Autowired
    lateinit var placeRepository: PlaceRepository

    @BeforeEach
    fun setup() {
        placeRepository.deleteAll()
        closedPlaceCandidateRepository.deleteAll()
    }

    @Test
    fun `등록된 장소와 동일한 정보가 폐업 되었다는 정보가 오면 폐업 장소 후보를 생성한다`() {
        val building = testDataGenerator.createBuilding()
        val placeName = "루나 아시아"
        val place = testDataGenerator.createPlace(placeName, building)
        val mockExternalId = UUID.randomUUID().toString()
        mockOpenData(mockExternalId, placeName, place.location)

        createClosedPlaceCandidatesUseCase.handle()

        val createdClosedPlaceCandidate = closedPlaceCandidateRepository.findByPlaceId(place.id)
        Assertions.assertNotNull(createdClosedPlaceCandidate)
        Assertions.assertTrue { createdClosedPlaceCandidate!!.externalId == mockExternalId }
    }

    @Test
    fun `이름이 달라도 충분히 비슷하고 장소가 일치하면 폐업 장소 후보를 생성한다`() {
        val building = testDataGenerator.createBuilding()
        val placeNameFromApiResponse = "루나아시아 - 삼성점"
        val targetPlace = testDataGenerator.createPlace("루나 아시아 삼성점", building)
        testDataGenerator.createPlace("전혀 다른 이름", building)
        val mockExternalId = UUID.randomUUID().toString()
        mockOpenData(mockExternalId, placeNameFromApiResponse, targetPlace.location)

        createClosedPlaceCandidatesUseCase.handle()

        val createdClosedPlaceCandidate = closedPlaceCandidateRepository.findByPlaceId(targetPlace.id)
        Assertions.assertNotNull(createdClosedPlaceCandidate)
        Assertions.assertTrue { createdClosedPlaceCandidate!!.externalId == mockExternalId }
    }

    @Test
    fun `해당 장소에 이미 폐업 추정 후보가 생성되어 있으면 중복 생성하지 않는다`() {
        val building = testDataGenerator.createBuilding()
        val placeName = "루나 아시아 삼성점"
        val place = testDataGenerator.createPlace(placeName, building)
        val mockExternalId = UUID.randomUUID().toString()
        mockOpenData(mockExternalId, placeName, place.location)

        createClosedPlaceCandidatesUseCase.handle()

        val createdClosedPlaceCandidate = closedPlaceCandidateRepository.findByPlaceId(place.id)
        Assertions.assertNotNull(createdClosedPlaceCandidate)
        Assertions.assertTrue { createdClosedPlaceCandidate!!.externalId == mockExternalId }

        createClosedPlaceCandidatesUseCase.handle()
        val closedPlaceCandidatesByExternalId = closedPlaceCandidateRepository.findByExternalIdIn(listOf(mockExternalId))
        Assertions.assertTrue { closedPlaceCandidatesByExternalId.size == 1 }
    }

    private fun mockOpenData(externalId: String, name: String, location: Location) {
        val fakeOpenDateResponse = listOf(
            ClosedPlaceResult(
                externalId = externalId,
                name = name,
                address = "가짜",
                postalCode = "21213",
                location = location,
                phoneNumber = null,
                closedDate = LocalDate.now(),
            )
        )
        doReturn(fakeOpenDateResponse).`when`(openDataService).getClosedPlaces()
    }
}

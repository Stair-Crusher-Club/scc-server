package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminClosedPlaceCandidateDTO
import club.staircrusher.admin_api.spec.dto.AdminListClosedPlaceCandidatesResponseDTO
import club.staircrusher.place.application.port.out.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.domain.model.ClosedPlaceCandidate
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.infra.adapter.`in`.controller.base.PlaceITBase
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import java.time.Duration
import java.util.UUID

class AdminPlaceControllerTest : PlaceITBase() {

    @Autowired
    lateinit var mockSccClock: MockSccClock

    @Autowired
    private lateinit var closedPlaceCandidateRepository: ClosedPlaceCandidateRepository

    @BeforeEach
    fun setUp() {
        closedPlaceCandidateRepository.deleteAll()
    }

    @Test
    fun `폐업 추정 장소 리스트 조회`() {
        val (_, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()

        // register 를 먼저 하는데도 closedPlaceCandidate 의 createdAt 이 request 를 날렸을 때 생성되는
        // initial TimestampCursor 의 value 보다 미래라서 item 이 empty 로 나온다. 따라서 advanceTIme 을 해준다
        mockSccClock.advanceTime(Duration.ofMinutes(2L))

        mvc
            .sccAdminRequest("/admin/closed-place-candidates", HttpMethod.GET, null)
            .run {
                val result = getResult(AdminListClosedPlaceCandidatesResponseDTO::class)

                Assertions.assertNotNull(result.items)
                Assertions.assertTrue { result.items!!.isNotEmpty() }

                val candidate = result.items!!.find { it.id == closedPlaceCandidate.id }
                println(candidate)
                Assertions.assertNotNull(candidate)
                Assertions.assertEquals(closedPlaceCandidate.placeId, candidate!!.placeId)
            }
    }

    @Test
    fun `폐업 추정 장소 단건 조회`() {
        val (place, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()

        mvc
            .sccAdminRequest("/admin/closed-place-candidates/${closedPlaceCandidate.id}", HttpMethod.GET, null)
            .run {
                val result = getResult(AdminClosedPlaceCandidateDTO::class)

                Assertions.assertEquals(closedPlaceCandidate.placeId, result.placeId)
                Assertions.assertEquals(place.name, result.name)
            }
    }

    @Test
    fun `폐업 추정 장소를 폐업 상태로 확정하기`() {
        val (place, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()

        mvc
            .sccAdminRequest("/admin/closed-place-candidates/${closedPlaceCandidate.id}/accept", HttpMethod.PUT, null)
            .run {
                val result = getResult(AdminClosedPlaceCandidateDTO::class)

                Assertions.assertEquals(closedPlaceCandidate.placeId, result.placeId)
                Assertions.assertEquals(place.name, result.name)
                Assertions.assertNotNull(result.acceptedAt)

                transactionManager.doInTransaction {
                    val candidateEntity = closedPlaceCandidateRepository.findByIdOrNull(closedPlaceCandidate.id)
                    Assertions.assertNotNull(candidateEntity!!.acceptedAt)
                    Assertions.assertNull(candidateEntity.ignoredAt)
                }
            }
    }

    @Test
    fun `폐업 추정 장소 무시하기`() {
        val (place, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()

        mvc
            .sccAdminRequest("/admin/closed-place-candidates/${closedPlaceCandidate.id}/ignore", HttpMethod.PUT, null)
            .run {
                val result = getResult(AdminClosedPlaceCandidateDTO::class)

                Assertions.assertEquals(closedPlaceCandidate.placeId, result.placeId)
                Assertions.assertEquals(place.name, result.name)
                Assertions.assertNotNull(result.ignoredAt)

                transactionManager.doInTransaction {
                    val candidateEntity = closedPlaceCandidateRepository.findByIdOrNull(closedPlaceCandidate.id)
                    Assertions.assertNotNull(candidateEntity!!.ignoredAt)
                    Assertions.assertNull(candidateEntity.acceptedAt)
                }
            }
    }


    private fun registerPlaceAndClosedPlaceCandidate(): Pair<Place, ClosedPlaceCandidate> = transactionManager.doInTransaction {
        val building = testDataGenerator.createBuilding()
        val place = testDataGenerator.createPlace(building = building)
        val closedPlaceCandidate = closedPlaceCandidateRepository.save(
            ClosedPlaceCandidate(
                id = UUID.randomUUID().toString(),
                placeId = place.id,
                externalId = UUID.randomUUID().toString(),
            )
        )

        place to closedPlaceCandidate
    }
}

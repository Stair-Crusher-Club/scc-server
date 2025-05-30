package club.staircrusher.place.infra.adapter.`in`.controller.place

import club.staircrusher.admin_api.spec.dto.AdminClosedPlaceCandidateDTO
import club.staircrusher.admin_api.spec.dto.AdminListClosedPlaceCandidatesResponseDTO
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.application.port.out.place.persistence.ClosedPlaceCandidateRepository
import club.staircrusher.place.domain.model.place.ClosedPlaceCandidate
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.place.infra.adapter.`in`.controller.place.base.PlaceITBase
import club.staircrusher.stdlib.clock.SccClock
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
    private lateinit var closedPlaceCandidateRepository: ClosedPlaceCandidateRepository

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @BeforeEach
    fun setUp() {
        closedPlaceCandidateRepository.deleteAll()
    }

    @Test
    fun `폐업 추정 장소 리스트 조회`() {
        val (_, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()
        val (_, ignoredPlaceCandidate) = registerPlaceAndIgnoreClosedPlaceCandidate()

        // register 를 먼저 하는데도 closedPlaceCandidate 의 createdAt 이 request 를 날렸을 때 생성되는
        // initial TimestampCursor 의 value 보다 미래라서 item 이 empty 로 나온다. 따라서 advanceTIme 을 해준다
        // annotation 으로 처리되는 createdAt 과 SccClock.instant 로 가져오는 시각의 차이가 조금 있는 듯
        clock.advanceTime(Duration.ofSeconds(1L))

        mvc
            .sccAdminRequest("/admin/closed-place-candidates", HttpMethod.GET, null)
            .run {
                val result = getResult(AdminListClosedPlaceCandidatesResponseDTO::class)

                Assertions.assertNotNull(result.items)
                Assertions.assertTrue { result.items!!.isNotEmpty() }

                val candidate = result.items!!.find { it.id == closedPlaceCandidate.id }
                Assertions.assertNotNull(candidate)
                Assertions.assertEquals(closedPlaceCandidate.placeId, candidate!!.placeId)

                val ignoredCandidate = result.items!!.find { it.id == ignoredPlaceCandidate.id }
                Assertions.assertNull(ignoredCandidate)
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

    @Test
    fun `폐업 상태로 확정할 때 접근성 정보가 등록되어 있다면, 함께 삭제된다`() {
        val (place, closedPlaceCandidate) = registerPlaceAndClosedPlaceCandidate()
        val placeAccessibilityId = testDataGenerator.registerPlaceAccessibility(place).id

        mvc
            .sccAdminRequest("/admin/closed-place-candidates/${closedPlaceCandidate.id}/accept", HttpMethod.PUT, null)
            .run {
                val result = getResult(AdminClosedPlaceCandidateDTO::class)

                Assertions.assertEquals(closedPlaceCandidate.placeId, result.placeId)
                Assertions.assertEquals(place.name, result.name)
                Assertions.assertNotNull(result.acceptedAt)

                transactionManager.doInTransaction {
                    val candidateEntity = closedPlaceCandidateRepository.findByIdOrNull(closedPlaceCandidate.id)!!
                    val placeAccessibility = placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)

                    Assertions.assertNotNull(candidateEntity.acceptedAt)
                    Assertions.assertNull(candidateEntity.ignoredAt)
                    Assertions.assertNull(placeAccessibility)
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
                originalName = place.name,
                originalAddress = place.address.toString(),
                closedAt = SccClock.instant(),
            )
        )

        place to closedPlaceCandidate
    }

    private fun registerPlaceAndIgnoreClosedPlaceCandidate(): Pair<Place, ClosedPlaceCandidate> = transactionManager.doInTransaction {
        val building = testDataGenerator.createBuilding()
        val place = testDataGenerator.createPlace(building = building)
        val closedPlaceCandidate = closedPlaceCandidateRepository.save(
            ClosedPlaceCandidate(
                id = UUID.randomUUID().toString(),
                placeId = place.id,
                externalId = UUID.randomUUID().toString(),
                originalName = place.name,
                originalAddress = place.address.toString(),
                closedAt = SccClock.instant(),
                ignoredAt = SccClock.instant(),
            )
        )

        place to closedPlaceCandidate
    }
}

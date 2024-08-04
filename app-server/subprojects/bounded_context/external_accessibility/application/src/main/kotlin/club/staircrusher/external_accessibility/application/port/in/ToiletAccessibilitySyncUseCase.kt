package club.staircrusher.external_accessibility.application.port.`in`

import club.staircrusher.external_accessibility.application.port.out.web.ToiletInfoFetcher
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.external_accessibility.domain.model.ToiletAccessibilityDetails
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory
import club.staircrusher.stdlib.geography.Location
import java.time.Instant

@Component
class ToiletAccessibilitySyncUseCase(
    private val externalAccessibilityService: ExternalAccessibilityService,
    private val toiletInfoFetcher: ToiletInfoFetcher,
) {

    fun load() {
        val records = toiletInfoFetcher.fetchRecords()
        externalAccessibilityService.upsert(
            records.map {
                ExternalAccessibility(
                    id = it.toiletId,
                    name = it.toiletName,
                    location = Location(it.longitude, it.latitude),
                    address = it.addressNew ?: it.addressOld ?: "",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    category = ExternalAccessibilityCategory.TOILET,
                    toiletDetails = ToiletAccessibilityDetails(
                        imageUrl = it.imageUrl,
                        gender = it.이용성별,
                        accessDesc = it.추천접근로,
                        availableDesc = it.사용가능여부,
                        entranceDesc = it.화장실입구구조,
                        stallWidth = it.내부가로너비,
                        stallDepth = it.내부세로너비,
                        doorDesc = it.대변기출입문,
                        doorSideRoom = it.대변기옆공간,
                        washStandBelowRoom = it.세면대아래공간,
                        washStandHandle = it.세면대손잡이,
                        extraDesc = it.기타참고사항,
                    )
                )
            }
        )

    }
}

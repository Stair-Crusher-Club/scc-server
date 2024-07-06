package club.staircrusher.external_accessibility.infra.adapter.out.persistence.sqldelight

import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.external_accessibility.domain.model.ToiletAccessibilityDetails
import club.staircrusher.infra.persistence.sqldelight.migration.External_accessibility
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.time.toOffsetDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import java.time.Instant


fun External_accessibility.toDomainModel(): ExternalAccessibility = ExternalAccessibility(
    id = id,
    name = name,
    location = Location(
        lat = location_x,
        lng = location_y,
    ),
    address = address,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    category = category,
    toiletDetails = details?.let {
        // TODO: 새로운 카테고리가 생기면 대응하기
        runCatching { decodeFromString<ToiletAccessibilityDetails>(it) }.getOrNull()
    }
)

fun ExternalAccessibility.toPersistenceModel(): External_accessibility = External_accessibility(
    id = id,
    name = name,
    location_x = location.lat,
    location_y = location.lng,
    address = address,
    created_at = Instant.now().toOffsetDateTime(),
    updated_at = Instant.now().toOffsetDateTime(),
    category = category,
    details = toiletDetails?.let {
        // TODO: 새로운 카테고리가 생기면 대응하기
        runCatching { Json.encodeToString(it) }.getOrNull()
    },
)

package club.staircrusher.external_accessibility.infra.adapter.out.persistence.sqldelight

import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.infra.persistence.sqldelight.migration.External_accessibility
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant


fun External_accessibility.toDomainModel(): ExternalAccessibility = ExternalAccessibility(
    id = id,
    name = name,
    latitude = location_x,
    longitude = location_y,
    address = address,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
)

fun ExternalAccessibility.toPersistenceModel(): External_accessibility = External_accessibility(
    id = id,
    name = name,
    location_x = latitude,
    location_y = longitude,
    address = address,
    created_at = Instant.now().toOffsetDateTime(),
    updated_at = Instant.now().toOffsetDateTime(),
)

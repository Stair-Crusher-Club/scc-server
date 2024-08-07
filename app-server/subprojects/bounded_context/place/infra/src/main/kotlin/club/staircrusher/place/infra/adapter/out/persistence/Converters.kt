package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import club.staircrusher.infra.persistence.sqldelight.migration.Building
import club.staircrusher.infra.persistence.sqldelight.migration.Place
import club.staircrusher.infra.persistence.sqldelight.migration.Place_favorite
import club.staircrusher.infra.persistence.sqldelight.query.place.FindById
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant

fun Building.toDomainModel(): club.staircrusher.place.domain.model.Building {
    val address = BuildingAddress(
        siDo = address_si_do,
        siGunGu = address_si_gun_gu,
        eupMyeonDong = address_eup_myeon_dong,
        li = address_li,
        roadName = address_road_name,
        mainBuildingNumber = address_main_building_number,
        subBuildingNumber = address_sub_building_number,
    )

    return club.staircrusher.place.domain.model.Building(
        id = id,
        name = name,
        location = Location(location_x, location_y),
        address = address,
        siGunGuId = si_gun_gu_id,
        eupMyeonDongId = eup_myeon_dong_id,
    )
}

fun club.staircrusher.place.domain.model.Building.toPersistenceModel(): Building {
    return Building(
        id = id,
        name = name,
        location_x = location.lng,
        location_y = location.lat,
        address_si_do = address.siDo,
        address_si_gun_gu = address.siGunGu,
        address_eup_myeon_dong = address.eupMyeonDong,
        address_li = address.li,
        address_road_name = address.roadName,
        address_main_building_number = address.mainBuildingNumber,
        address_sub_building_number = address.subBuildingNumber,
        si_gun_gu_id = siGunGuId,
        eup_myeon_dong_id = eupMyeonDongId,
        created_at = Instant.now().toOffsetDateTime(),
        updated_at = Instant.now().toOffsetDateTime(),
    )
}

fun Place.toDomainModel(
    building: club.staircrusher.place.domain.model.Building,
): club.staircrusher.place.domain.model.Place {
    return club.staircrusher.place.domain.model.Place(
        id = id,
        name = name,
        location = Location(lng = location_x, lat = location_y),
        building = building,
        siGunGuId = si_gun_gu_id,
        eupMyeonDongId = eup_myeon_dong_id,
        category = category,
        isClosed = is_closed,
        isNotAccessible = is_not_accessible,
    )
}

fun FindById.toDomainModel(): club.staircrusher.place.domain.model.Place {
    val address = BuildingAddress(
        siDo = address_si_do,
        siGunGu = address_si_gun_gu,
        eupMyeonDong = address_eup_myeon_dong,
        li = address_li,
        roadName = address_road_name,
        mainBuildingNumber = address_main_building_number,
        subBuildingNumber = address_sub_building_number,
    )
    val building = club.staircrusher.place.domain.model.Building(
        id = id_,
        name = name_,
        location = Location(location_x_, location_y_),
        address = address,
        siGunGuId = si_gun_gu_id_,
        eupMyeonDongId = eup_myeon_dong_id_,
    )

    return club.staircrusher.place.domain.model.Place(
        id = id,
        name = name,
        location = Location(lng = location_x, lat = location_y),
        building = building,
        siGunGuId = si_gun_gu_id,
        eupMyeonDongId = eup_myeon_dong_id,
        category = category,
        isClosed = is_closed,
        isNotAccessible = is_not_accessible,
    )
}

fun club.staircrusher.place.domain.model.Place.toPersistenceModel(): Place {
    return Place(
        id = id,
        name = name,
        location_x = location.lng,
        location_y = location.lat,
        building_id = building.id,
        si_gun_gu_id = siGunGuId,
        eup_myeon_dong_id = eupMyeonDongId,
        category = category,
        created_at = Instant.now().toOffsetDateTime(),
        updated_at = Instant.now().toOffsetDateTime(),
        is_closed = isClosed,
        is_not_accessible = isNotAccessible,
    )
}

fun PlaceFavorite.toPersistenceModel(): Place_favorite {
    return Place_favorite(
        id = id,
        place_id = placeId,
        user_id = userId,
        created_at = Instant.now().toOffsetDateTime(),
        updated_at = Instant.now().toOffsetDateTime(),
        deleted_at = deletedAt?.toOffsetDateTime()
    )
}

fun Place_favorite.toDomainModel(): PlaceFavorite {
    return PlaceFavorite(
        id = id,
        placeId = place_id,
        userId = user_id,
        createdAt = created_at.toInstant(),
        updatedAt = updated_at.toInstant(),
        deletedAt = deleted_at?.toInstant()
    )
}

fun PlaceFavorite.toDto(): club.staircrusher.api.spec.dto.PlaceFavorite {
    return club.staircrusher.api.spec.dto.PlaceFavorite(
        placeId = placeId,
        userId = userId,
        createdAt = EpochMillisTimestamp(updatedAt.toEpochMilli()),
    )
}


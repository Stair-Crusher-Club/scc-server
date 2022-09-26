package club.staircrusher.quest.infra.adapter.`in`.converter

import club.staircrusher.admin.api.dto.LocationDTO
import club.staircrusher.stdlib.geography.Location

object LocationConverter {
    fun convertToModel(dto: LocationDTO): Location {
        return Location(lng = dto.lng, lat = dto.lat)
    }

    fun convertToDTO(model: Location): LocationDTO {
        return LocationDTO(lng = model.lng, lat = model.lat)
    }
}

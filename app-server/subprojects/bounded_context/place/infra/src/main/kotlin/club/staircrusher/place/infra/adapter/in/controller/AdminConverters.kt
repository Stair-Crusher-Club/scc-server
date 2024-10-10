package club.staircrusher.place.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.place.application.result.NamedClosedPlaceCandidate

fun NamedClosedPlaceCandidate.toAdminDTO() = club.staircrusher.admin_api.spec.dto.AdminClosedPlaceCandidateDTO(
    id = candidateId,
    placeId = placeId,
    name = name,
    address = address,
    acceptedAt = acceptedAt?.toDTO(),
    ignoredAt = ignoredAt?.toDTO(),
)

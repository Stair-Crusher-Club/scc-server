package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminChallengeActionConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeActionConditionTypeEnumDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeAddressConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeDTO
import club.staircrusher.admin_api.spec.dto.AdminCreateChallengeRequestDTO
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddressCondition
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.domain.model.CreateChallengeRequest

fun Challenge.toAdminDTO() = AdminChallengeDTO(
    id = id,
    name = name,
    isPublic = isPublic,
    invitationCode = invitationCode,
    passcode = passcode,
    isComplete = isComplete,
    startsAtMillis = startsAt.toEpochMilli(),
    endsAtMillis = endsAt?.toEpochMilli(),
    goal = goal,
    milestones = milestones,
    conditions = conditions.map { it.toAdminDTO() },
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    description = description,
)

fun AdminCreateChallengeRequestDTO.toModel() = CreateChallengeRequest(
    name = name,
    isPublic = isPublic,
    invitationCode = invitationCode,
    passcode = passcode,
    challengeCrusherGroup = challengeCrusherGroup?.let { group ->
        ChallengeCrusherGroup(
            icon = group.icon?.let { icon ->
                ChallengeCrusherGroup.Icon(
                    url = icon.url,
                    width = icon.width,
                    height = icon.height,
                )
            },
            name = group.name
        )
    },
    startsAtMillis = startsAtMillis,
    endsAtMillis = endsAtMillis,
    goal = goal,
    milestones = milestones,
    conditions = conditions.map { it.toModel() },
    description = description,
)

fun AdminChallengeConditionDTO.toModel() = ChallengeCondition(
    addressCondition = addressCondition?.toModel(),
    actionCondition = actionCondition?.toModel(),
)

fun ChallengeCondition.toAdminDTO() = AdminChallengeConditionDTO(
    addressCondition = addressCondition?.toAdminDTO(),
    actionCondition = actionCondition?.toAdminDTO(),
)

fun AdminChallengeAddressConditionDTO.toModel() = ChallengeAddressCondition(
    rawEupMyeonDongs = rawEupMyeonDongs?.takeIf { it.isNotEmpty() }, // default는 전체다.
)

fun ChallengeAddressCondition.toAdminDTO() = AdminChallengeAddressConditionDTO(
    rawEupMyeonDongs = rawEupMyeonDongs,
)

fun AdminChallengeActionConditionDTO.toModel() = ChallengeActionCondition(
    types = types?.takeIf { it.isNotEmpty() }?.map { it.toModel() } ?: ChallengeActionCondition.Type.values()
        .toList(), // default는 전체다.
)

fun ChallengeActionCondition.toAdminDTO() = AdminChallengeActionConditionDTO(
    types = types.map { it.toModel() },
)

fun AdminChallengeActionConditionTypeEnumDTO.toModel() = when (this) {
    AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY -> ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY
    AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY_COMMENT -> ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY_COMMENT
    AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY -> ChallengeActionCondition.Type.PLACE_ACCESSIBILITY
    AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY_COMMENT -> ChallengeActionCondition.Type.PLACE_ACCESSIBILITY_COMMENT
}

fun ChallengeActionCondition.Type.toModel() = when (this) {
    ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY -> AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY
    ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY_COMMENT -> AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY_COMMENT
    ChallengeActionCondition.Type.PLACE_ACCESSIBILITY -> AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY
    ChallengeActionCondition.Type.PLACE_ACCESSIBILITY_COMMENT -> AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY_COMMENT
}

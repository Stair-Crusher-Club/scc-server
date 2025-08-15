package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminChallengeActionConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeActionConditionTypeEnumDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeAddressConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeConditionDTO
import club.staircrusher.admin_api.spec.dto.AdminChallengeDTO
import club.staircrusher.admin_api.spec.dto.AdminCreateChallengeRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminCrusherGroupDto
import club.staircrusher.admin_api.spec.dto.AdminCrusherGroupDtoIcon
import club.staircrusher.admin_api.spec.dto.AdminUpdateChallengeRequestDTO
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeActionCondition
import club.staircrusher.challenge.domain.model.ChallengeAddressCondition
import club.staircrusher.challenge.domain.model.ChallengeCondition
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.domain.model.CreateChallengeRequest
import club.staircrusher.challenge.domain.model.UpdateChallengeRequest
import java.time.Instant

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
    crusherGroup = crusherGroup?.toAdminDTO(),
)

fun AdminCreateChallengeRequestDTO.toModel() = CreateChallengeRequest(
    name = name,
    isPublic = isPublic,
    invitationCode = invitationCode,
    passcode = passcode,
    isB2B = false, // TODO: Admin API에서 B2B 챌린지 지원 시 추가.
    startsAtMillis = startsAtMillis,
    endsAtMillis = endsAtMillis,
    goal = goal,
    milestones = milestones,
    conditions = conditions.map { it.toModel() },
    quests = null, // TODO: Admin API에서 퀘스트 지원 시 추가
    description = description,
    crusherGroup = crusherGroup?.toModel(),
)

fun AdminUpdateChallengeRequestDTO.toModel(challengeId: String) = UpdateChallengeRequest(
    id = challengeId,
    name = name,
    endsAt = endsAtMillis?.let { Instant.ofEpochMilli(it) },
    description = description,
    crusherGroup = crusherGroup?.toModel(),
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
    AdminChallengeActionConditionTypeEnumDTO.PLACE_REVIEW -> ChallengeActionCondition.Type.PLACE_REVIEW
}

fun ChallengeActionCondition.Type.toModel() = when (this) {
    ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY -> AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY
    ChallengeActionCondition.Type.BUILDING_ACCESSIBILITY_COMMENT -> AdminChallengeActionConditionTypeEnumDTO.BUILDING_ACCESSIBILITY_COMMENT
    ChallengeActionCondition.Type.PLACE_ACCESSIBILITY -> AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY
    ChallengeActionCondition.Type.PLACE_ACCESSIBILITY_COMMENT -> AdminChallengeActionConditionTypeEnumDTO.PLACE_ACCESSIBILITY_COMMENT
    ChallengeActionCondition.Type.PLACE_REVIEW -> AdminChallengeActionConditionTypeEnumDTO.PLACE_REVIEW
}

fun AdminCrusherGroupDto.toModel() = ChallengeCrusherGroup(
    name = name,
    icon = icon?.let {
        ChallengeCrusherGroup.Icon(
            url = it.url,
            width = it.width,
            height = it.height,
        )
    }
)

fun ChallengeCrusherGroup.toAdminDTO() = AdminCrusherGroupDto(
    name = name,
    icon = icon?.let {
        AdminCrusherGroupDtoIcon(
            url = it.url,
            width = it.width,
            height = it.height,
        )
    }
)

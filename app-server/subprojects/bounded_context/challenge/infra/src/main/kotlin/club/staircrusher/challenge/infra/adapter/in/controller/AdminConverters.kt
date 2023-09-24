package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminChallengeDTO
import club.staircrusher.admin_api.spec.dto.AdminCreateChallengeRequestDTO
import club.staircrusher.challenge.domain.model.Challenge
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
    conditions = conditions,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
)

fun AdminCreateChallengeRequestDTO.toModel() = CreateChallengeRequest(
    name = name,
    isPublic = isPublic,
    invitationCode = invitationCode,
    passcode = passcode,
    startsAtMillis = startsAtMillis,
    endsAtMillis = endsAtMillis,
    goal = goal,
    milestones = milestones,
)

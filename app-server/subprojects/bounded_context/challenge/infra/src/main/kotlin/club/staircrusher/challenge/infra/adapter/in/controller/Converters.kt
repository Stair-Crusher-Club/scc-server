package club.staircrusher.challenge.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ChallengeCrusherGroupDto
import club.staircrusher.api.spec.dto.ChallengeDto
import club.staircrusher.api.spec.dto.ChallengeRankDto
import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import club.staircrusher.api.spec.dto.ImageWithMetadataDto
import club.staircrusher.api.spec.dto.ListChallengesItemDto
import club.staircrusher.challenge.domain.model.ChallengeCrusherGroup
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.challenge.domain.model.ChallengeStatus
import java.time.Instant

fun ChallengeCrusherGroup.toDTO(): ChallengeCrusherGroupDto = ChallengeCrusherGroupDto(
    name = name,
    icon = icon?.let { ic ->
        ImageWithMetadataDto(
            url = ic.url,
            width = ic.width,
            height = ic.height,
        )
    }
)

fun ChallengeStatus.toDto(): ChallengeStatusDto = when (this) {
    ChallengeStatus.UPCOMING -> ChallengeStatusDto.UPCOMING
    ChallengeStatus.IN_PROGRESS -> ChallengeStatusDto.IN_PROGRESS
    ChallengeStatus.CLOSED -> ChallengeStatusDto.CLOSED
}

fun club.staircrusher.challenge.domain.model.Challenge.toDto(
    participationsCount: Int,
    contributionsCount: Int,
    criteriaTime: Instant
) = ChallengeDto(
    id = id,
    name = name,
    status = getStatus(criteriaTime).toDto(),
    isPublic = isPublic,
    isComplete = isComplete,
    startsAt = EpochMillisTimestamp(startsAt.toEpochMilli()),
    endsAt = endsAt?.let { EpochMillisTimestamp(it.toEpochMilli()) },
    goal = goal,
    milestones = milestones,
    participationsCount = participationsCount,
    contributionsCount = contributionsCount,
    description = description,
)

fun club.staircrusher.challenge.domain.model.Challenge.toListChallengeDto(hasJoined: Boolean, criteriaTime: Instant) =
    ListChallengesItemDto(
        id = id,
        name = name,
        status = getStatus(criteriaTime).toDto(),
        startsAt = EpochMillisTimestamp(startsAt.toEpochMilli()),
        endsAt = endsAt?.toEpochMilli()?.let { EpochMillisTimestamp(it) },
        hasJoined = hasJoined,
        createdAt = EpochMillisTimestamp(createdAt.toEpochMilli()),
    )

fun ChallengeRank.toDto(nickname: String) = ChallengeRankDto(
    contributionCount = contributionCount,
    rank = rank,
    nickname = nickname,
)

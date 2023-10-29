@file:Suppress("TooManyFunctions")

package club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight

import club.staircrusher.api.spec.dto.ChallengeDto
import club.staircrusher.api.spec.dto.ChallengeRankDto
import club.staircrusher.api.spec.dto.ChallengeStatusDto
import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import club.staircrusher.api.spec.dto.ListChallengesItemDto
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.challenge.domain.model.ChallengeStatus
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_contribution
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_participation
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_rank
import club.staircrusher.infra.persistence.sqldelight.query.challenge.FindByUidAndTime
import club.staircrusher.stdlib.time.toOffsetDateTime
import java.time.Instant


fun Challenge.toDomainModel() = club.staircrusher.challenge.domain.model.Challenge(
    id = id,
    name = name,
    isPublic = is_public,
    invitationCode = invitation_code,
    passcode = passcode,
    isComplete = is_complete,
    startsAt = starts_at.toInstant(),
    endsAt = ends_at?.toInstant(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

fun club.staircrusher.challenge.domain.model.Challenge.toPersistenceModel() = Challenge(
    id = id,
    name = name,
    is_public = isPublic,
    invitation_code = invitationCode,
    passcode = passcode,
    is_complete = isComplete,
    starts_at = startsAt.toOffsetDateTime(),
    ends_at = endsAt?.toOffsetDateTime(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime()
)

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
    contributionsCount = contributionsCount
)

fun ChallengeStatus.toDto(): ChallengeStatusDto = when (this) {
    ChallengeStatus.UPCOMING -> ChallengeStatusDto.upcoming
    ChallengeStatus.IN_PROGRESS -> ChallengeStatusDto.inProgress
    ChallengeStatus.CLOSED -> ChallengeStatusDto.closed
}

fun Challenge_contribution.toDomainModel() = ChallengeContribution(
    id = id,
    userId = user_id,
    challengeId = challenge_id,
    placeAccessibilityId = place_accessibility_id,
    placeAccessibilityCommentId = place_accessibility_comment_id,
    buildingAccessibilityId = building_accessibility_id,
    buildingAccessibilityCommentId = building_accessibility_comment_id,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

fun ChallengeContribution.toPersistenceModel() = Challenge_contribution(
    id = id,
    user_id = userId,
    challenge_id = challengeId,
    place_accessibility_id = placeAccessibilityId,
    place_accessibility_comment_id = placeAccessibilityCommentId,
    building_accessibility_id = buildingAccessibilityId,
    building_accessibility_comment_id = buildingAccessibilityCommentId,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime()
)

fun Challenge_participation.toDomainModel() = ChallengeParticipation(
    id = id,
    challengeId = challenge_id,
    userId = user_id,
    createdAt = created_at.toInstant()
)

fun ChallengeParticipation.toPersistenceModel() = Challenge_participation(
    id = id,
    challenge_id = challengeId,
    user_id = userId,
    created_at = createdAt.toOffsetDateTime()
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

fun FindByUidAndTime.toChallenge() = club.staircrusher.challenge.domain.model.Challenge(
    id = id,
    name = name,
    isPublic = is_public,
    invitationCode = invitation_code,
    passcode = passcode,
    isComplete = is_complete,
    startsAt = starts_at.toInstant(),
    endsAt = ends_at?.toInstant(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

fun ChallengeRank.toPersistenceModel() = Challenge_rank(
    id = id,
    challenge_id = challengeId,
    user_id = userId,
    contribution_count = contributionCount,
    rank = rank,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime()
)

fun Challenge_rank.toDomainModel() = ChallengeRank(
    id = id,
    challengeId = challenge_id,
    userId = user_id,
    contributionCount = contribution_count,
    rank = rank,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

fun ChallengeRank.toDto(nickname: String) = ChallengeRankDto(
    contributionCount = contributionCount,
    rank = rank,
    nickname = nickname,
)

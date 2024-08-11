@file:Suppress("TooManyFunctions")

package club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight

import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_contribution
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_participation
import club.staircrusher.infra.persistence.sqldelight.migration.Challenge_rank
import club.staircrusher.infra.persistence.sqldelight.query.challenge.FindByUidAndTime
import club.staircrusher.stdlib.time.toOffsetDateTime

fun Challenge.toDomainModel() = club.staircrusher.challenge.domain.model.Challenge(
    id = id,
    name = name,
    isPublic = is_public,
    invitationCode = invitation_code,
    passcode = passcode,
    crusherGroup = crusher_group,
    isComplete = is_complete,
    startsAt = starts_at.toInstant(),
    endsAt = ends_at?.toInstant(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    description = description,
)

fun club.staircrusher.challenge.domain.model.Challenge.toPersistenceModel() = Challenge(
    id = id,
    name = name,
    is_public = isPublic,
    invitation_code = invitationCode,
    passcode = passcode,
    crusher_group = crusherGroup,
    is_complete = isComplete,
    starts_at = startsAt.toOffsetDateTime(),
    ends_at = endsAt?.toOffsetDateTime(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime(),
    description = description,
)

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

fun FindByUidAndTime.toChallenge() = club.staircrusher.challenge.domain.model.Challenge(
    id = id,
    name = name,
    isPublic = is_public,
    invitationCode = invitation_code,
    passcode = passcode,
    crusherGroup = crusher_group,
    isComplete = is_complete,
    startsAt = starts_at.toInstant(),
    endsAt = ends_at?.toInstant(),
    goal = goal,
    milestones = milestones,
    conditions = conditions,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    description = description,
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

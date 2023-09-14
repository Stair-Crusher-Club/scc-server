package club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight

import club.staircrusher.infra.persistence.sqldelight.migration.Challenge
import club.staircrusher.stdlib.time.toOffsetDateTime

fun Challenge.toDomainModel() = club.staircrusher.challenge.domain.model.Challenge(
    id = id,
    name = name,
    isPublic = is_public,
    invitationCode = invitation_code,
    passcode = passcode,
    isComplete = is_complete,
    startsAt = starts_at.toInstant(),
    endsAt = ends_at?.toInstant(),
    goals = goals,
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
    goals = goals,
    conditions = conditions,
    created_at = createdAt.toOffsetDateTime(),
    updated_at = updatedAt.toOffsetDateTime()
)

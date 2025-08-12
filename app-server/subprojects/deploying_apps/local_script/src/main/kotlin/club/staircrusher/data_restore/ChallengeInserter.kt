package club.staircrusher.data_restore

import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.readCsvAsLines
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


fun main() {
    val paLines = readCsvAsLines("data_restore/scc_public_place_accessibility_6.csv")
    val baLines = readCsvAsLines("data_restore/scc_public_building_accessibility_3.csv")

    val pas = paLines.map {
        PA(
            id = it[0],
            userId = it[1],
            createdAt = it[2].toInstant(),
        )
    }

    val bas = baLines.map {
        BA(
            id = it[0],
            userId = it[1],
            createdAt = it[2].toInstant()
        )
    }

    val challengeParticipations = pas.map {
        ChallengeContribution(
            id = EntityIdGenerator.generateRandom(),
            userId = it.userId,
            challengeId = "d62c9604-9eee-4fda-81d1-66813ca1daae",
            placeAccessibilityId = it.id,
            placeAccessibilityCommentId = null,
            buildingAccessibilityId = null,
            buildingAccessibilityCommentId = null,
            placeReviewId = null,
            createdAt = it.createdAt,
            updatedAt = it.createdAt,
        )
    } + bas.map {
        ChallengeContribution(
            id = EntityIdGenerator.generateRandom(),
            userId = it.userId,
            challengeId = "d62c9604-9eee-4fda-81d1-66813ca1daae",
            placeAccessibilityId = null,
            placeAccessibilityCommentId = null,
            buildingAccessibilityId = it.id,
            buildingAccessibilityCommentId = null,
            placeReviewId = null,
            createdAt = it.createdAt,
            updatedAt = it.createdAt,
        )
    }

    challengeParticipations.forEach {
        println(it.toInsertQuery())
    }
}

data class PA(
    val id: String,
    val userId: String,
    val createdAt: Instant,
)

data class BA(
    val id: String,
    val userId: String,
    val createdAt: Instant,
)

private fun String.toInstant(): Instant {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS XXX", Locale.ENGLISH)
    val odt = OffsetDateTime.parse(this, formatter)
    return odt.toInstant()
}

private fun Instant.toQueryString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    return this.atZone(ZoneId.of("UTC")).format(formatter)
}

private  fun ChallengeContribution.toInsertQuery(): String {
    val placeAccessibilityStr = if (placeAccessibilityId != null) {
        "'$placeAccessibilityId'"
    } else {
        "NULL"
    }
    val buildingAccessibilityStr = if (buildingAccessibilityId != null) {
        "'$buildingAccessibilityId'"
    } else {
        "NULL"
    }

    return "INSERT INTO challenge_contribution VALUES (" +
        "'$id'," + // id
        "'$userId'," + //user_id
        "'$challengeId'," + // challenge_id
        "$placeAccessibilityStr," + // place_accessibility_id
        "NULL," + // place_accessibility_comment_id
        "$buildingAccessibilityStr," + // building_accessibility_id
        "NULL," + // building_accessibility_comment_id
        "'${createdAt.toQueryString()}'," + // created_at
        "'${createdAt.toQueryString()}'," + // updated_at
        "NULL" + // place_review_id
        ");"
}

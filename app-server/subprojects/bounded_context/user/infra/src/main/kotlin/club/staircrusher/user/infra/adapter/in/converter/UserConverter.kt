package club.staircrusher.user.infra.adapter.`in`.converter

import club.staircrusher.user.domain.model.UserProfile

fun UserProfile.toDTO() = club.staircrusher.api.spec.dto.User(
    id = userId,
    nickname = nickname,
    instagramId = instagramId,
    email = email,
    mobilityTools = mobilityTools.map { it.toDTO() },
    birthYear = birthYear,
    isNewsLetterSubscriptionAgreed = isNewsLetterSubscriptionAgreed,
)

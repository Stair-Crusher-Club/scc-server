package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.persistence.jpa.NoArgsConstructor

@NoArgsConstructor
data class ChallengeCrusherGroup(
    val icon: Icon?,
    val name: String
) {
    @NoArgsConstructor
    data class Icon(
        val url: String,
        val width: Int,
        val height: Int
    )
}

package club.staircrusher.challenge.domain.model

data class ChallengeCrusherGroup(
    val icon: Icon?,
    val name: String
) {
    data class Icon(
        val url: String,
        val width: Int,
        val height: Int
    )
}

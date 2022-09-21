package club.staircrusher.place_search.domain.model

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val building: Building,
)
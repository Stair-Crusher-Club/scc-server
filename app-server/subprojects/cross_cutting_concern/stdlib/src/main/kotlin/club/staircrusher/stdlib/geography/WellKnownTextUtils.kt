package club.staircrusher.stdlib.geography

object WellKnownTextUtils {
    fun convertToPolygonWkt(locations: List<Location>): String {
        check(locations.isNotEmpty())
        return buildString {
            append("POLYGON((")
            locations.forEach {
                append("${it.lng} ${it.lat}, ")
            }
            locations.first().let {
                append("${it.lng} ${it.lat}") // 폐쇄된 point 목록이어야 한다.
            }
            append("))")
        }
    }
}

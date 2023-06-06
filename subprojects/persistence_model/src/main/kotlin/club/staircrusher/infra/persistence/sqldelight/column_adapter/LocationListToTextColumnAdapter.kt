package club.staircrusher.infra.persistence.sqldelight.column_adapter

import club.staircrusher.stdlib.geography.Location

object LocationListToTextColumnAdapter : ListToTextColumnAdapter<Location>() {
    override fun convertElementToTextColumn(element: Location) = with(element) { "$lng/$lat" }
    override fun convertElementFromTextColumn(text: String): Location {
        val (lngStr, latStr) = text.split("/")
        return Location(lngStr.toDouble(), latStr.toDouble())
    }
}

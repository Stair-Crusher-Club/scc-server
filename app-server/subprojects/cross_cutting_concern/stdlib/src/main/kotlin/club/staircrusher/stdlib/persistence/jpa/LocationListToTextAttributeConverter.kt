package club.staircrusher.stdlib.persistence.jpa

import club.staircrusher.stdlib.geography.Location
import jakarta.persistence.Converter

@Converter
object LocationListToTextAttributeConverter : ListToTextAttributeConverter<Location>() {
    override fun convertElementToTextColumn(element: Location): String {
        return with(element) { "$lng/$lat" }
    }

    override fun convertElementFromTextColumn(text: String): Location {
        val (lngStr, latStr) = text.split("/")
        return Location(lngStr.toDouble(), latStr.toDouble())
    }
}

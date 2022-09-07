package club.staircrusher.stdlib.geography

import org.geotools.referencing.GeodeticCalculator

object LocationUtils {
    fun calculateDistance(l1: Location, l2: Location): Length {
        val calculator = GeodeticCalculator()
        calculator.setStartingGeographicPoint(l1.lng, l1.lat)
        calculator.setDestinationGeographicPoint(l2.lng, l2.lat)
        return Length(calculator.orthodromicDistance)
    }
}

package club.staircrusher.stdlib.geography

import org.geotools.referencing.GeodeticCalculator

object LocationUtils {
    fun calculateDistance(l1: Location, l2: Location): Length {
        val calculator = GeodeticCalculator()
        calculator.setStartingGeographicPoint(l1.lng, l1.lat)
        calculator.setDestinationGeographicPoint(l2.lng, l2.lat)
        return Length(calculator.orthodromicDistance)
    }

    /**
     * https://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon#answer-2922778
     */
    fun isInPolygon(polygonVertices: List<Location>, testLocation: Location): Boolean {
        val edges = polygonVertices.zip(polygonVertices.subList(1, polygonVertices.size) + polygonVertices[0])
        var result = false
        edges.forEach { (v1, v2) ->
            if ((v1.lat > testLocation.lat) != (v2.lat > testLocation.lat) &&
                (testLocation.lng < (v2.lng - v1.lng) * (testLocation.lat - v1.lat) / (v2.lat - v1.lat) + v1.lng)) {
                result = !result
            }
        }
        return result
    }
}

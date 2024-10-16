package club.staircrusher.stdlib.geography

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

class CrsConverter(
    sourceCrsType: CrsType,
    targetCrsType: CrsType,
) {
    private val crsFactory = CRSFactory()
    private val transformFactory = CoordinateTransformFactory()

    private val sourceCrs = crsFactory.createFromParameters(sourceCrsType.wellKnownName, sourceCrsType.proj)
    private val targetCrs = crsFactory.createFromParameters(targetCrsType.wellKnownName, targetCrsType.proj)
    private val transformer = transformFactory.createTransform(sourceCrs, targetCrs)

    fun toLocation(x: Double, y: Double): Location {
        val sourceCoordinate = ProjCoordinate(x, y)
        val transformedCoordinate = transformer.transform(sourceCoordinate, ProjCoordinate())

        return Location(
            lat = transformedCoordinate.y,
            lng = transformedCoordinate.x,
        )
    }
}

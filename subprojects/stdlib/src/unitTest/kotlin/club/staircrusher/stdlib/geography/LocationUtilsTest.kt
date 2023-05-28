package club.staircrusher.stdlib.geography

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocationUtilsTest {
    @Test
    fun `isInPolygon - 정사각형 polygon`() {
        val boundaryVertices = listOf(
            Location(1.0, 1.0),
            Location(1.0, 2.0),
            Location(2.0, 2.0),
            Location(2.0, 1.0),
        )
        val testLocation1 = Location(1.5, 1.5)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation1))
        val testLocation2 = Location(1.05, 1.95)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation2))
        val testLocation3 = Location(1.95, 1.95)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation3))

        val testLocation4 = Location(-1.0, -1.0)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation4))
        val testLocation5 = Location(0.0, 1.5)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation5))
        val testLocation6 = Location(2.05, 1.05)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation6))

        // boundary에 거의 근접한 경우 테스트; 완전 boundary는 예외가 좀 있다고 해서 테스트하지 않는다.
        val testLocation7 = Location(1.00001, 1.5)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation7))
        val testLocation8 = Location(1.5, 1.99999)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation8))
        val testLocation9 = Location(1.99999, 1.5)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation9))
        val testLocation10 = Location(1.5, 1.00001)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation10))
    }

    @Test
    fun `isInPolygon - 마름모 polygon`() {
        val boundaryVertices = listOf(
            Location(0.0, 1.0),
            Location(1.0, 2.0),
            Location(2.0, 1.0),
            Location(1.0, 0.0),
        )
        val testLocation1 = Location(1.0, 1.0)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation1))
        val testLocation2 = Location(1.0, 0.05)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation2))
        val testLocation3 = Location(1.95, 1.0)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation3))

        val testLocation4 = Location(0.0, 0.00)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation4))
        val testLocation5 = Location(0.0, 1.05)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation5))
        val testLocation6 = Location(1.55, 1.55)
        assertFalse(LocationUtils.isInPolygon(boundaryVertices, testLocation6))

        // boundary에 거의 근접한 경우 테스트; 완전 boundary는 예외가 좀 있다고 해서 테스트하지 않는다.
        val testLocation7 = Location(0.50001, 0.50001)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation7))
        val testLocation8 = Location(0.50001, 1.49999)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation8))
        val testLocation9 = Location(1.49999, 1.49999)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation9))
        val testLocation10 = Location(1.49999, 0.50001)
        assertTrue(LocationUtils.isInPolygon(boundaryVertices, testLocation10))
    }
}

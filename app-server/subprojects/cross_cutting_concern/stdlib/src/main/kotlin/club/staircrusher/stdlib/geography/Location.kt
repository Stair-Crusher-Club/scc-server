@file:Suppress("MagicNumber")

package club.staircrusher.stdlib.geography

import at.kopyk.CopyExtensions
import jakarta.persistence.Embeddable
import kotlin.math.abs

@CopyExtensions
@Embeddable
data class Location(
    val lng: Double,
    val lat: Double,
) {
    fun plusLng(length: Length) = copy(
        lng = lng + length.toLngDiff(this)
    )

    fun minusLng(length: Length) = copy(
        lng = lng - length.toLngDiff(this)
    )

    fun plusLat(length: Length) = copy(
        lat = lat + length.toLatDiff()
    )

    fun minusLat(length: Length) = copy(
        lat = lat - length.toLatDiff()
    )
}

/**
 * length를 longitude와 latitude 차이로 환산하는 함수.
 * 적도에서 위/경도 0.00001 차이 = 1.11m임을 기반으로 계산한다.
 * - latitude 차이 - longitude가 어디든 상관 없이 1m = 0.00001 / 1.11이다.
 * - longitude 차이 - 적도에서 남/북극에 가까워질수록 1m가 만들어내는 차이가 커지는데, 구의 모양을 고려하면 (90 / (90 - |latitude|))에 정비례하여 커진다.
 */
private fun Length.toLngDiff(location: Location): Double {
    return (0.00001 / 1.11) * (90 / (90 - abs(location.lat))) * meter
}

private fun Length.toLatDiff(): Double {
    return (0.00001 / 1.11) * meter
}

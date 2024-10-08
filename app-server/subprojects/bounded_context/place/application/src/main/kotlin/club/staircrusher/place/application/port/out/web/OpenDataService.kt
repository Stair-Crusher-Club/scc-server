package club.staircrusher.place.application.port.out.web

import club.staircrusher.place.application.result.ClosedPlaceResult

interface OpenDataService {
    fun getClosedPlaces(): List<ClosedPlaceResult>
}

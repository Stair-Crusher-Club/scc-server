package club.staircrusher.place.application.port.out.place.web

import club.staircrusher.place.application.result.ClosedPlaceResult

interface OpenDataService {
    fun getClosedPlaces(): List<ClosedPlaceResult>
}

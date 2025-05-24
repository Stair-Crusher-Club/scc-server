package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.Image
import org.springframework.data.repository.CrudRepository

interface ImageRepository : CrudRepository<Image, String> {
}

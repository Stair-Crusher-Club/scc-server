package club.staircrusher.place.application.port.out.accessibility.persistence.place_review

import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReview
import org.springframework.data.repository.CrudRepository

interface PlaceReviewRepository : CrudRepository<PlaceReview, String>

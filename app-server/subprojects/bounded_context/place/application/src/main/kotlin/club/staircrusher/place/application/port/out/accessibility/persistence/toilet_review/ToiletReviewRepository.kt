package club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review

import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReview
import org.springframework.data.repository.CrudRepository

interface ToiletReviewRepository : CrudRepository<ToiletReview, String>

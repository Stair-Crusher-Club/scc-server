package club.staircrusher.place.domain.model.accessibility.toilet_review

import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id

@Entity
class ToiletReview(
    @Id
    val id: String = EntityIdGenerator.generateRandom(),

    @Enumerated(EnumType.STRING)
    val toiletLocationType: ToiletLocationType,

    val targetId: String,

    @Embedded
    val detail: ToiletReviewDetail?,

    @Column(columnDefinition = "text")
    val comment: String,
) : TimeAuditingBaseEntity() {
    init {
        when (toiletLocationType) {
            ToiletLocationType.PLACE,
            ToiletLocationType.BUILDING -> {
                checkNotNull(detail) { "매장 내 있음 혹은 건물 내 있음을 선택한 경우, 장애인 화장실에 대한 상세 정보가 있어야 합니다." }
            }
            ToiletLocationType.NONE,
            ToiletLocationType.NOT_SURE,
            ToiletLocationType.ETC -> { /* Do nothing */ }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ToiletReview) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ToiletReview(id='$id', toiletLocationType=$toiletLocationType, targetId='$targetId', detail=$detail, comment='$comment')"
    }
}

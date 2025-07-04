package club.staircrusher.place.domain.model.accessibility.toilet_review

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where

@Entity
class ToiletReview(
    @Id
    val id: String = EntityIdGenerator.generateRandom(),

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Enumerated(EnumType.STRING)
    val toiletLocationType: ToiletLocationType,

    @Column(name = "target_id", nullable = false)
    val targetId: String,

    @Column(nullable = true)
    val floor: Int?,

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    val entranceDoorTypes: List<EntranceDoorType>?,

    @OneToMany(mappedBy = "accessibilityId", fetch = FetchType.EAGER)
    @Where(clause = "accessibility_type = 'ToiletReview'")
    @OrderBy("displayOrder asc")
    var images: MutableList<AccessibilityImage> = mutableListOf(),

    @Column(columnDefinition = "text")
    val comment: String?,
) : TimeAuditingBaseEntity() {
    init {
        when (toiletLocationType) {
            ToiletLocationType.PLACE,
            ToiletLocationType.BUILDING -> {
                check(
                    floor != null && entranceDoorTypes != null
                ) { "매장 내 있음 혹은 건물 내 있음을 선택한 경우, 장애인 화장실에 대한 상세 정보가 있어야 합니다." }
            }
            ToiletLocationType.NONE,
            ToiletLocationType.NOT_SURE -> { /* Do nothing */ }
            ToiletLocationType.ETC -> {
                checkNotNull(comment) { "기타를 선택한 경우, 장애인 화장실에 대한 설명이 있어야 합니다." }
            }
        }
    }

    fun isDeletable(uid: String?): Boolean {
        return uid != null && uid == userId
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
        return "ToiletReview(id='$id', toiletLocationType=$toiletLocationType, targetId='$targetId', floor=$floor, entranceDoorTypes=$entranceDoorTypes, images=$images, comment='$comment')"
    }
}

package club.staircrusher.place.domain.model.check

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(ImageInspectionResult.Visible::class),
    JsonSubTypes.Type(ImageInspectionResult.NotVisible::class),
)
sealed class ImageInspectionResult {
    abstract val rotation: Rotation

    data class Visible(
        val objects: List<DetectedObject>,
        override val rotation: Rotation
    ) : ImageInspectionResult()

    object NotVisible : ImageInspectionResult() {
        override val rotation: Rotation
            get() = Rotation.D0
    }

    enum class DetectedObject {
        Elevator,
        Entrance,
        Stair,
    }

    enum class Rotation {
        D0,
        D90,
        D180,
        D270,
    }
}

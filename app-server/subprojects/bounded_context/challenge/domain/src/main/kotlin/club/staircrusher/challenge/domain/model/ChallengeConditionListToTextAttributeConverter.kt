package club.staircrusher.challenge.domain.model

import club.staircrusher.stdlib.jpa.ListToTextAttributeConverter

class ChallengeConditionListToTextAttributeConverter : ListToTextAttributeConverter<ChallengeCondition>() {
    override fun convertElementToTextColumn(element: ChallengeCondition): String {
        return objectMapper.writeValueAsString(element)
    }

    override fun convertElementFromTextColumn(text: String): ChallengeCondition {
        return objectMapper.readValue(text, ChallengeCondition::class.java)
    }
}

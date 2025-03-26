package club.staircrusher.user.infra.adapter.out.web.subscription.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono

internal interface StibeeApiClient {
    // https://help.stibee.com/api-webhook/list-api#h_5c2983c86c
    @PostExchange(
        url = "/lists/{listId}/subscribers",
        contentType = "application/json",
        accept = ["application/json"],
    )
    fun registerSubscriber(
        @PathVariable("listId") listId: String,
        @RequestBody body: RegisterSubscriberRequestDto,
    ): Mono<RegisterSubscriberResponseDto>

    @DeleteExchange(
        url = "/lists/{listId}/subscribers",
        contentType = "application/json",
        accept = ["application/json"],
    )
    fun unregisterSubscriber(
        @PathVariable("listId") listId: String,
        @RequestBody body: UnregisterSubscriberRequestDto,
    ): Mono<UnregisterSubscriberResponseDto>

    @Serializable
    data class RegisterSubscriberRequestDto(
        /**
         * 구독자 정보를 담고 있습니다. Key-Value 배열 형식으로 구성됩니다.
         */
        @SerialName("subscribers")
        val subscribers: List<Subscriber>,
        /**
         * 구독자를 추가한 방법을 구분합니다.
         * "MANUAL": 관리자에 의해 추가한 것으로 기록합니다. 구독중인 구독자는 업데이트 됩니다.(기본값)
         * "SUBSCRIBER": 구독자가 직접 구독한 것으로 기록합니다. 구독중인 구독자는 업데이트 되지 않습니다
         */
        @SerialName("eventOccuredBy")
        val eventOccurredBy: String? = null,
        /**
         * confirmEmailYN: 구독 확인 이메일 발송 여부를 구분합니다.
         * "Y": 구독자에게 구독 확인 이메일을 발송하고, 구독자가 이 메일을 통해 구독 확인을 해야 구독자로 추가됩니다. 이 때 한 번에 추가하는 구독자가 10명을 초과하면 추가되지 않습니다.
         * "N": 구독 확인 과정없이 바로 구독자로 추가됩니다. (기본값)
         */
        @SerialName("confirmEmailYN")
        val confirmEmailYN: String? = null,
        /**
         * groupIds: 그룹에 할당된 고유의 아이디입니다. 해당 그룹에 구독자를 할당합니다.(기본값: 할당안함)
         */
        @SerialName("groupIds")
        val groupIds: List<String>? = null,
    ) {
        @Serializable
        data class Subscriber(
            @SerialName("email")
            val email: String,

            @SerialName("name")
            val name: String,

            @SerialName("\$ad_agreed")
            val isMarketingPushAgreed: Boolean,
        )
    }

    @Serializable
    data class UnregisterSubscriberRequestDto(
        @SerialName("subscribers")
        val subscribers: List<String>,
    )

    @Serializable
    data class RegisterSubscriberResponseDto(
        @SerialName("Ok")
        val isOk: Boolean,

        // Stibee 의 경우 에러가 나더라도 http code 는 200 으로 주고 Error 객체를 채워서 주는 듯 하다
        @SerialName("Error")
        val error: Error? = null,

        @SerialName("Value")
        val result: Value,
    ) {
        @Serializable
        data class Error(
            @SerialName("Code")
            val code: String,

            @SerialName("Message")
            val message: String,
        )

        @Serializable
        data class Subscriber(
            @SerialName("email")
            val email: String,

            @SerialName("name")
            val name: String,

            @SerialName("stb_ad_agreement")
            val isMarketingPushAgreed: Boolean,

            @SerialName("\$type")
            val type: String,

            @SerialName("\$status")
            val status: String,

            @SerialName("\$createdTime")
            val createdAt: String,

            @SerialName("\$modifiedTime")
            val updatedAt: String,
        )

        @Serializable
        data class Value(
            @SerialName("success")
            val registeredSubscribers: List<Subscriber>,

            @SerialName("update")
            val updatedSubscribers: List<Subscriber>,

            @JsonNames("failDuplicatedEmail", "failDuplicatedPhone", "failExistEmail", "failExistPhone", "failNoEmail", "failUnknown", "failValidation", "failValidationDateTime", "failWrongEmail", "failWrongPhone")
            val failedSubscribers: List<Subscriber>,
        )
    }

    @Serializable
    data class UnregisterSubscriberResponseDto(
        @SerialName("Ok")
        val isOk: Boolean,

        @SerialName("Error")
        val error: Error? = null,

        @SerialName("Value")
        val result: Value,
    ) {
        @Serializable
        data class Error(
            @SerialName("Code")
            val code: String,

            @SerialName("Message")
            val message: String,
        )

        @Serializable
        data class Value(
            @SerialName("success")
            val unregisteredSubscribers: List<String>,

            @SerialName("fail")
            val failedSubscribers: List<String>,
        )
    }
}

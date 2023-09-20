package club.staircrusher.spring_web.web

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.stdlib.domain.SccDomainException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// TODO: error 내려주기 위한 api spec 정의
@ControllerAdvice
class SccExceptionHandler {
    private val logger = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(t: Throwable): ResponseEntity<String> {
        return when (t) {
            is SccDomainException -> {
                logger.info(t) { "Bad Request: $t" }
                if (t.errorCode != null) {
                    ResponseEntity
                        .badRequest()
                        // FIXME: 하위호환을 위해 ResponseEntity<ApiErrorResponse>를 내려주는 대신 ResponseEntity<String>으로 내려준다.
                        //        이후 ApiErrorResponse를 클라가 이해하게 되면 전부 ApiErrorResponse를 내려주도록 변경한다.
                        .body(objectMapper.writeValueAsString(t.toApiErrorResponse()))
                } else {
                    ResponseEntity
                        .badRequest()
                        .body(t.msg)
                }
            }

            is HttpRequestMethodNotSupportedException,
            is HttpMediaTypeNotSupportedException,
            is HttpMessageNotReadableException,
            is MissingKotlinParameterException,
            -> {
                logger.info(t) { "Bad Request: ${t.message}" }
                return ResponseEntity
                    .badRequest()
                    .body(t.message)
            }

            else -> {
                logger.error(t) { "Unexpected Error: ${t.message}" } // 일단은 모든 에러를 ERROR 레벨로 찍고, 불필요한 에러를 제외하는 식으로 간다.
                ResponseEntity
                    .internalServerError()
                    .body("알 수 없는 에러가 발생했습니다. 다시 시도해주세요.")
            }
        }
    }

    private fun SccDomainException.toApiErrorResponse(): ApiErrorResponse {
        return ApiErrorResponse(
            msg = msg,
            code = when (errorCode) {
                SccDomainException.ErrorCode.INVALID_AUTHENTICATION -> ApiErrorResponse.Code.INVALID_AUTHENTICATION
                SccDomainException.ErrorCode.INVALID_NICKNAME -> ApiErrorResponse.Code.INVALID_NICKNAME
                SccDomainException.ErrorCode.INVALID_EMAIL -> ApiErrorResponse.Code.INVALID_EMAIL
                SccDomainException.ErrorCode.INVALID_PASSCODE -> ApiErrorResponse.Code.INVALID_PASSCODE
                SccDomainException.ErrorCode.ALREADY_JOINED -> ApiErrorResponse.Code.ALREADY_JOINED
                SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED -> ApiErrorResponse.Code.CHALLENGE_NOT_OPENED
                SccDomainException.ErrorCode.CHALLENGE_CLOSED -> ApiErrorResponse.Code.CHALLENGE_CLOSED
                null -> null
            }
        )
    }
}

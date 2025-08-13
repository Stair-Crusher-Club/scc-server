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

@ControllerAdvice
class SccExceptionHandler {
    private val logger = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(t: Throwable): ResponseEntity<String> {
        return when (t) {
            is SccDomainException -> {
                logger.info(t) { "Bad Request: $t, cause: ${t.cause}" }
                ResponseEntity
                    .badRequest()
                    .body(objectMapper.writeValueAsString(t.toApiErrorResponse()))
            }

            is IllegalArgumentException -> {
                logger.info(t) { "Bad Request: $t, cause: ${t.cause}" }
                ResponseEntity
                    .badRequest()
                    .body(objectMapper.writeValueAsString(t.toApiErrorResponse()))
            }

            is HttpRequestMethodNotSupportedException,
            is HttpMediaTypeNotSupportedException,
            is HttpMessageNotReadableException,
            is MissingKotlinParameterException,
                -> {
                logger.info(t) { "Bad Request: ${t.message}" }
                return ResponseEntity
                    .badRequest()
                    .body(objectMapper.writeValueAsString(ApiErrorResponse(msg = t.message)))
            }

            else -> {
                logger.error(t) { "Unexpected Error: ${t.message}" } // 일단은 모든 에러를 ERROR 레벨로 찍고, 불필요한 에러를 제외하는 식으로 간다.
                ResponseEntity
                    .internalServerError()
                    .body(objectMapper.writeValueAsString(ApiErrorResponse(msg = "알 수 없는 에러가 발생했습니다. 다시 시도해주세요.")))
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
                SccDomainException.ErrorCode.INVALID_BIRTH_YEAR -> ApiErrorResponse.Code.INVALID_BIRTH_YEAR
                SccDomainException.ErrorCode.INVALID_PASSCODE -> ApiErrorResponse.Code.INVALID_PASSCODE
                SccDomainException.ErrorCode.B2B_INFO_REQUIRED -> ApiErrorResponse.Code.B2B_INFO_REQUIRED
                SccDomainException.ErrorCode.INVALID_ARGUMENTS -> ApiErrorResponse.Code.INVALID_ARGUMENTS
                SccDomainException.ErrorCode.ALREADY_JOINED -> ApiErrorResponse.Code.ALREADY_JOINED
                SccDomainException.ErrorCode.CHALLENGE_NOT_OPENED -> ApiErrorResponse.Code.CHALLENGE_NOT_OPENED
                SccDomainException.ErrorCode.CHALLENGE_CLOSED -> ApiErrorResponse.Code.CHALLENGE_CLOSED
                null -> null
            }
        )
    }

    private fun IllegalArgumentException.toApiErrorResponse(): ApiErrorResponse {
        return ApiErrorResponse(
            msg = message ?: "잘못된 요청입니다.",
            code = null,
        )
    }
}

package club.staircrusher.spring_web.web

import club.staircrusher.stdlib.domain.SccDomainException
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// TODO: error 내려주기 위한 api spec 정의
@ControllerAdvice
class SccExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler
    fun handle(e: SccDomainException): ResponseEntity<String>{
        logger.info(e) { "Bad Request: ${e.msg}" }
        return ResponseEntity
            .badRequest()
            .body(e.msg)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(t: Throwable): ResponseEntity<String> {
        logger.error(t) { "Unexpected Error: ${t.message}" } // 일단은 모든 에러를 ERROR 레벨로 찍고, 불필요한 에러를 제외하는 식으로 간다.
        return ResponseEntity
            .internalServerError()
            .body("알 수 없는 에러가 발생했습니다. 다시 시도해주세요.")
    }
}

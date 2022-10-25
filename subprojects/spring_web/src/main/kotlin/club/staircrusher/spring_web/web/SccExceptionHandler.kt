package club.staircrusher.spring_web.web

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// TODO: exception 별로 세분화
// TODO: error 내려주기 위한 api spec 정의
// TODO: 센트리 연동
@ControllerAdvice
class SccExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(t: Throwable): ResponseEntity<String> {
        logger.error(t) { t.message }
        val errorMessage = buildString {
            var cause: Throwable? = t
            var isInitial = true
            while (cause != null) {
                if (!isInitial) {
                    appendLine()
                    append("  Caused by ")
                }
                append("${cause::class.simpleName}: ${cause.message}")
                cause = cause.cause
                isInitial = false
            }
        }
        return ResponseEntity
            .badRequest()
            .body(errorMessage)
    }
}

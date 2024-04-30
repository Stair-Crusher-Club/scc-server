package club.staircrusher.spring_web.logging

import club.staircrusher.spring_web.logging.SccLoggingFilter.Companion.SCC_LOGGING_FILTER_ORDER
import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID

@Component
@Order(SCC_LOGGING_FILTER_ORDER)
class SccLoggingFilter: OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = UUID.randomUUID().toString().substring(REQUEST_ID_LENGTH)

        // TODO: logback 에서 로그 request_id 로 묶기
        MDC.put(REQUEST_ID, requestId)

        val cachingRequestWrapper = ContentCachingRequestWrapper(request)
        val cachingResponseWrapper = ContentCachingResponseWrapper(response)

        filterChain.doFilter(cachingRequestWrapper, cachingResponseWrapper)

        try {
            val logMessage = HttpLogMessage(
                method = cachingRequestWrapper.method,
                uri = cachingRequestWrapper.requestURI,
                status = HttpStatus.valueOf(cachingResponseWrapper.status),
                requestBody = objectMapper.readTree(cachingRequestWrapper.contentAsByteArray).toString().ifBlank { "Empty" },
                responseBody = objectMapper.readTree(cachingResponseWrapper.contentAsByteArray).toString().ifBlank { "Empty" },
            )

            if (logMessage.status == HttpStatus.OK) {
                // 최소 logging level 이 info 이므로 trace 로 설정해서 안보이게 한다
                log.trace(logMessage.toString())
            } else {
                log.info(logMessage.toString())
            }
        } catch (e: Throwable) {
            log.error(e) { "[${this::class::simpleName} failed to log" }
        } finally {
            cachingResponseWrapper.copyBodyToResponse()
            MDC.remove(REQUEST_ID)
        }
    }

    companion object {
        const val REQUEST_ID = "request_id"
        const val SCC_LOGGING_FILTER_ORDER = Int.MIN_VALUE
        private const val REQUEST_ID_LENGTH = 8
    }
}

package club.staircrusher.spring_web.logging

import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID

@Component
open class SccLoggingFilter: OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = UUID.randomUUID().toString().substring(0, REQUEST_ID_LENGTH)
        MDC.put(REQUEST_ID, requestId)

        val requestWrapper = wrapRequest(request)
        val responseWrapper = wrapResponse(response)
        try {
            filterChain.doFilter(requestWrapper, responseWrapper)
        } finally {
            if (isAsyncStarted(requestWrapper).not()) {
                logRequestResponse(requestWrapper, responseWrapper)
                responseWrapper.copyBodyToResponse()
            }
        }

        MDC.remove(REQUEST_ID)
    }

    override fun shouldNotFilterAsyncDispatch(): Boolean {
        return false
    }

    private fun logRequestResponse(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper) {
        try {
            val requestBody = if (request.contentLength > 0) {
                objectMapper.readTree(request.contentAsByteArray).toString().ifBlank { null }
            } else {
                null
            }

            val responseBody = if (response.contentSize > 0) {
                objectMapper.readTree(response.contentAsByteArray).toString().ifBlank { null }
            } else {
                null
            }

            val logMessage = HttpLogMessage(
                method = request.method,
                uri = request.requestURI,
                status = HttpStatus.valueOf(response.status),
                requestBody = requestBody,
                responseBody = responseBody,
            )

            if (logMessage.status == HttpStatus.OK) {
                // 최소 logging level 이 info 이므로 trace 로 설정해서 안보이게 한다
                log.trace(logMessage.toString())
            } else {
                log.info(logMessage.toString())
            }
        } catch (e: Throwable) {
            log.error(e) { "[SccLoggingFilter] failed to log" }
        }
    }

    private fun wrapRequest(request: HttpServletRequest): ContentCachingRequestWrapper {
        return if (request is ContentCachingRequestWrapper) {
            request
        } else {
            ContentCachingRequestWrapper(request)
        }
    }

    private fun wrapResponse(response: HttpServletResponse): ContentCachingResponseWrapper {
        return if (response is ContentCachingResponseWrapper) {
            response
        } else {
            ContentCachingResponseWrapper(response)
        }
    }

    companion object {
        const val REQUEST_ID = "request_id"
        private const val REQUEST_ID_LENGTH = 8
    }
}

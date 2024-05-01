package club.staircrusher.spring_web.logging

import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.lang.Exception

@Component
class SccLoggingInterceptor: HandlerInterceptor {
    private val logger = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            val cachedRequest = request as ContentCachingRequestWrapper
            val cachedResponse = response as ContentCachingResponseWrapper

            logRequestResponse(cachedRequest, cachedResponse)
        } catch (e: Throwable) {
            logger.info(e) { "[SccLoggingInterceptor] failed to log" }
        }
    }

    private fun logRequestResponse(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper) {
        // Request 의 body 는 Application 에서 읽어야 ContentCaching~ 에 캐싱된다
        // suspend 함수는 Dispatch Type 이 REQUEST 로 처음 들어왔다가 ASYNC 로 여러번 처리될 수 있다
        // 결국 로깅은 마지막 ASYNC 가 처리된 이후에 하게 되는데 그때는 doFilter 안에서
        // request body 를 읽지 않았다면 caching 되어 있는 request body 가 없어서 null 이 찍힌다
        val requestBody = objectMapper.readTree(request.contentAsByteArray).toString().ifBlank { null }
        val responseBody = objectMapper.readTree(response.contentAsByteArray).toString().ifBlank { null }

        val logMessage = HttpLogMessage(
            method = request.method,
            uri = request.requestURI,
            status = HttpStatus.valueOf(response.status),
            requestBody = requestBody,
            responseBody = responseBody,
        )

        if (logMessage.status.is2xxSuccessful) {
            // 최소 logging level 이 info 이므로 trace 로 설정해서 안보이게 한다
            logger.trace(logMessage.toString())
        } else {
            logger.info(logMessage.toString())
        }
    }
}

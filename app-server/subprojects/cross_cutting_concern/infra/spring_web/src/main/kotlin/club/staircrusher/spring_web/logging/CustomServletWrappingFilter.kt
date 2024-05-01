package club.staircrusher.spring_web.logging

import club.staircrusher.stdlib.di.annotation.Component
import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID

@Component
open class CustomServletWrappingFilter: OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val isFirstRequest = isAsyncDispatch(request).not()
        if (isFirstRequest) {
            val requestId = UUID.randomUUID().toString().substring(0, REQUEST_ID_LENGTH)
            MDC.put(REQUEST_ID, requestId)
        }
        val requestToUse = wrapRequest(request)
        val responseToUse = wrapResponse(response)

        filterChain.doFilter(requestToUse, responseToUse)

        if (isAsyncStarted(requestToUse)) {
            requestToUse.asyncContext.addListener(
                object : AsyncListener {
                    override fun onComplete(asyncEvent: AsyncEvent) {
                        responseToUse.copyBodyToResponse()
                        MDC.remove(REQUEST_ID)
                    }

                    override fun onTimeout(event: AsyncEvent?) {
                        // No-op
                    }

                    override fun onError(event: AsyncEvent?) {
                        //No-op
                    }

                    override fun onStartAsync(event: AsyncEvent?) {
                        //No-op
                    }
                }
            )
        } else {
            responseToUse.copyBodyToResponse()
            MDC.remove(REQUEST_ID)
        }
    }

    override fun shouldNotFilterAsyncDispatch(): Boolean {
        return false
    }

    private fun wrapRequest(request: HttpServletRequest): ContentCachingRequestWrapper {
        return if (request is ContentCachingRequestWrapper) {
            return request
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

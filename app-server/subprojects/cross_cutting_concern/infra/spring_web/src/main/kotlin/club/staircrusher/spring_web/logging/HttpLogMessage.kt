package club.staircrusher.spring_web.logging

import org.springframework.http.HttpStatus

// Client IP 가 필요할지?
data class HttpLogMessage(
    val method: String,
    val uri: String,
    val status: HttpStatus,
    val requestBody: String?,
    val responseBody: String?,
) {
    override fun toString(): String {
        return "[${this.method} ${this.uri} ${this.status}] Request: ${this.requestBody}, Response: ${this.responseBody}"
    }
}

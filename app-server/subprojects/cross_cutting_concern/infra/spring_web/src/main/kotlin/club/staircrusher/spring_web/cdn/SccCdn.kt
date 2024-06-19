package club.staircrusher.spring_web.cdn

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.beans.factory.annotation.Value

@Component
open class SccCdn(
    @Value("\${scc.cloudfront.domain:#{null}") val domain: String?,
) {
    init {
        SccCdnBeanHolder.setIfNull(this)
    }

    @Suppress("ReturnCount")
    fun replaceIfPossible(url: String): String {
        if (domain == null) {
            return url
        }

        val objectKey = url.substringAfterLast(S3_DOMAIN)
        if (objectKey == url) {
            return url
        }

        return "https://$domain/$objectKey"
    }

    companion object {
        private const val S3_DOMAIN = "s3.amazonaws.com/"

        fun replaceIfPossible(url: String): String {
            val globalSccCdn = SccCdnBeanHolder.get()
            checkNotNull(globalSccCdn) {
                "Cannot use SccCdn.replaceIfPossible since SccCdn bean is not initialized yet."
            }
            return globalSccCdn.replaceIfPossible(url)
        }
    }
}

package club.staircrusher.spring_web.cdn

import club.staircrusher.stdlib.di.annotation.Component
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value

@Component
open class SccCdn(
    @Value("\${scc.cloudfront.domain:#{null}") val domain: String?,
) {
    private val logger = KotlinLogging.logger {}

    init {
        SccCdnBeanHolder.setIfNull(this)
    }

    @Suppress("ReturnCount")
    fun replaceIfPossible(url: String): String {
        logger.info("Replacing CDN URL: $url")
        if (domain == null) {
            return url
        }

        val objectKey = url.substringAfterLast(S3_DOMAIN)
        logger.info("Object key: $objectKey")
        if (objectKey == url) {
            return url
        }

        logger.info("Replaced URL: https://$domain/$objectKey")
        return "https://$domain/$objectKey"
    }

    companion object {
        private const val S3_DOMAIN = "amazonaws.com/"

        fun replaceIfPossible(url: String): String {
            val globalSccCdn = SccCdnBeanHolder.get()
            checkNotNull(globalSccCdn) {
                "Cannot use SccCdn.replaceIfPossible since SccCdn bean is not initialized yet."
            }
            return globalSccCdn.replaceIfPossible(url)
        }
    }
}

package club.staircrusher.spring_web.cdn

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.beans.factory.annotation.Value

@Component
open class SccCdn(
    @Value("\${scc.cloudfront.domain:#{null}}") val domain: String?,
    @Value("\${scc.s3.imageUpload.bucketName:scc-dev-accessibility-images-2}") val accessibilityImageBucketName: String,
) {
    init {
        SccCdnBeanHolder.setIfNull(this)
    }

    private val accessibilityImageS3Domain = "https://${accessibilityImageBucketName}.s3.ap-northeast-2.amazonaws.com/"

    @Suppress("ReturnCount")
    fun forAccessibilityImage(url: String): String {
        if (domain == null) {
            return url
        }

        val objectKey = url.substringAfterLast(accessibilityImageS3Domain)
        if (objectKey == url) {
            return url
        }

        return "https://$domain/$objectKey"
    }

    companion object {
        fun forAccessibilityImage(url: String): String {
            val globalSccCdn = SccCdnBeanHolder.get()
            checkNotNull(globalSccCdn) {
                "Cannot use SccCdn.replaceIfPossible since SccCdn bean is not initialized yet."
            }
            return globalSccCdn.forAccessibilityImage(url)
        }
    }
}

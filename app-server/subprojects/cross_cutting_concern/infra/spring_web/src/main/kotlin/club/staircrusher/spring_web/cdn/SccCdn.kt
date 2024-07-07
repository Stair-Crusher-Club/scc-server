package club.staircrusher.spring_web.cdn

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.beans.factory.annotation.Value

@Component
open class SccCdn(
    @Value("\${scc.cloudfront.domain:#{null}}") val domain: String?,
    @Value("\${scc.s3.imageUpload.bucketName:scc-dev-accessibility-images-2}") val accessibilityImageBucketName: String,
    @Value("\${scc.s3.imageUpload.thumbnailBucketName:scc-dev-accessibility-thumbnails}") val accessibilityThumbnailBucketName: String,
) {
    init {
        SccCdnBeanHolder.setIfNull(this)
    }

    private val accessibilityImageS3Domain = "https://${accessibilityImageBucketName}.s3.ap-northeast-2.amazonaws.com/"
    private val accessibilityThumbnailS3Domain = "https://${accessibilityThumbnailBucketName}.s3.ap-northeast-2.amazonaws.com/"

    @Suppress("ReturnCount")
    fun forAccessibilityImage(url: String): String {
        if (domain == null) {
            return url
        }

        val originalImageObjectKey = url.substringAfterLast(accessibilityImageS3Domain).takeIf { it != url }
        val thumbnailObjectKey = url.substringAfterLast(accessibilityThumbnailS3Domain).takeIf { it != url }

        val objectKeyToUse = originalImageObjectKey ?: thumbnailObjectKey
        if (objectKeyToUse == null) {
            return url
        }

        return "https://$domain/$objectKeyToUse"
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

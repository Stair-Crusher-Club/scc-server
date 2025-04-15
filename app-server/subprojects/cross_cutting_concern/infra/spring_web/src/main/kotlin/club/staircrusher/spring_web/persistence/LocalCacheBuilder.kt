package club.staircrusher.spring_web.persistence

import club.staircrusher.stdlib.di.annotation.Component
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics
import java.util.concurrent.TimeUnit

@Component
class LocalCacheBuilder(
    private val meterRegistry: MeterRegistry,
) {
    fun <K, V> build(name:String, maxSize: Long, expiresAfterSeconds: Long): Cache<K, V> {
        val localCache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expiresAfterSeconds, TimeUnit.SECONDS)
            .recordStats()
            .build<K, V>()

        GuavaCacheMetrics.monitor(meterRegistry, localCache, name)

        return localCache
    }
}

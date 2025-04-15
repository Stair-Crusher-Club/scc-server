package club.staircrusher.infra.network

import club.staircrusher.stdlib.di.annotation.Component
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketListener
import io.micrometer.core.instrument.MeterRegistry
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class RateLimiterFactory(
    private val meterRegistry: MeterRegistry,
) {
    fun create(
        name: String,
        limitPerSecond: Long,
    ): Bucket {
        val listener = MicrometerBucketListener(meterRegistry, name)
        val bandwidth = Bandwidth.builder()
            .capacity(limitPerSecond).refillGreedy(limitPerSecond, Duration.ofSeconds(1))
            .build()

        val bucket = Bucket.builder()
            .addLimit(bandwidth)
            .build()

        return bucket.toListenable(listener)
    }

    class MicrometerBucketListener(
        meterRegistry: MeterRegistry,
        name: String,
    ) : BucketListener {
        private val consumedCounter = meterRegistry.counter("rate.limiter.consumed", "name", name)
        private val rejectedCounter = meterRegistry.counter("rate.limiter.rejected", "name", name)
        private val parkedNanosTimer = meterRegistry.timer("rate.limiter.parked.nanos", "name", name)
        private val interruptedCounter = meterRegistry.counter("rate.limiter.interrupted", "name", name)
        private val delayedNanosTimer = meterRegistry.timer("rate.limiter.delayed.nanos", "name", name)

        override fun onConsumed(tokens: Long) {
            consumedCounter.increment(tokens.toDouble())
        }

        override fun onRejected(tokens: Long) {
            rejectedCounter.increment(tokens.toDouble())
        }

        override fun onParked(nanos: Long) {
            parkedNanosTimer.record(nanos, TimeUnit.NANOSECONDS)
        }

        override fun onInterrupted(e: InterruptedException?) {
            interruptedCounter.increment()
        }

        override fun onDelayed(nanos: Long) {
            delayedNanosTimer.record(nanos, TimeUnit.NANOSECONDS)
        }
    }
}

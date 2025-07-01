package club.staircrusher.stdlib.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

object SccDispatchers {
    // 메모리를 많이 사용하는 작업은 OOM 방지를 위해 동시 실행을 줄인다.
    @OptIn(ExperimentalCoroutinesApi::class)
    val ImageProcess = Dispatchers.Default.limitedParallelism(1)
}

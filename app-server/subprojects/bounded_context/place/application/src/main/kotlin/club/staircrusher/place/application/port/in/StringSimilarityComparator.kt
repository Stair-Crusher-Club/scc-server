package club.staircrusher.place.application.port.`in`

import kotlin.math.max
import kotlin.math.min

@Suppress("ComplexMethod", "NestedBlockDepth")
object StringSimilarityComparator {
    private const val N: Int = 2

    /**
     * https://github.com/tdebatty/java-string-similarity/blob/master/src/main/java/info/debatty/java/stringsimilarity/NGram.java
     * Compute n-gram distance.
     * @param s0 The first string to compare.
     * @param s1 The second string to compare.
     * @return The computed n-gram distance in the range [0, 1]
     */
    fun getSimilarity(s0: String, s1: String): Double {
        if (s0 == s1) {
            return 0.0
        }

        val special = '\n'
        val sl = s0.length
        val tl = s1.length

        if (sl == 0 || tl == 0) {
            return 1.0
        }

        var cost = 0
        if (sl < N || tl < N) {
            var i = 0
            val ni = min(sl.toDouble(), tl.toDouble()).toInt()
            while (i < ni) {
                if (s0[i] == s1[i]) {
                    cost++
                }
                i++
            }
            return cost.toFloat() / max(sl.toDouble(), tl.toDouble())
        }

        val sa = CharArray(sl + N - 1)
        var p: FloatArray //'previous' cost array, horizontally
        var d: FloatArray // cost array, horizontally
        var d2: FloatArray //placeholder to assist in swapping p and d

        //construct sa with prefix
        for (i in sa.indices) {
            if (i < N - 1) {
                sa[i] = special //add prefix
            } else {
                sa[i] = s0[i - N + 1]
            }
        }
        p = FloatArray(sl + 1)
        d = FloatArray(sl + 1)

        // indexes into strings s and t
        var i: Int // iterates through source

        var t_j = CharArray(N) // jth n-gram of t

        i = 0
        while (i <= sl) {
            p[i] = i.toFloat()
            i++
        }

        var j = 1 // iterates through target
        while (j <= tl) {
            //construct t_j n-gram
            if (j < N) {
                for (ti in 0 until N - j) {
                    t_j[ti] = special //add prefix
                }
                for (ti in N - j until N) {
                    t_j[ti] = s1[ti - (N - j)]
                }
            } else {
                t_j = s1.substring(j - N, j).toCharArray()
            }
            d[0] = j.toFloat()
            i = 1
            while (i <= sl) {
                cost = 0
                var tn = N
                //compare sa to t_j
                for (ni in 0 until N) {
                    if (sa[i - 1 + ni] != t_j[ni]) {
                        cost++
                    } else if (sa[i - 1 + ni] == special) {
                        //discount matches on prefix
                        tn--
                    }
                }
                val ec = cost.toFloat() / tn
                // minimum of cell to the left+1, to the top+1,
                // diagonally left and up +cost
                d[i] = min(
                    min((d[i - 1] + 1).toDouble(), (p[i] + 1).toDouble()), (p[i - 1] + ec).toDouble()
                )
                    .toFloat()
                i++
            }
            // copy current distance counts to 'previous row' distance counts
            d2 = p
            p = d
            d = d2
            j++
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[sl] / max(tl.toDouble(), sl.toDouble())
    }
}

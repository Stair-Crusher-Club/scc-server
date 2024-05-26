package club.staircrusher.stdlib.version

import club.staircrusher.stdlib.util.string.emptyToNull
import kotlin.math.min

/**
 * https://semver.org/
 * <valid semver> ::= <version core>
 *                  | <version core> "-" <pre-release>
 *                  | <version core> "+" <build>
 *                  | <version core> "-" <pre-release> "+" <build>
 * <version core> ::= <major> "." <minor> "." <patch>
 *
 * @property <major> ::= <numeric identifier>
 * @property <minor> ::= <numeric identifier>
 * @property <patch> ::= <numeric identifier>
 * @property <pre-release> ::= <pre-release identifier> | <pre-release identifier> "." <dot-separated pre-release identifiers>
 *           <pre-release identifier> ::= <alphanumeric identifier>| <numeric identifier>
 * @property <build> ::= <build identifier>| <build identifier> "." <dot-separated build identifiers>
 *           <build identifier> ::= <alphanumeric identifier>| <digits>
 * */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildMetadata: String? = null
) : Comparable<SemanticVersion> {

    fun nextMajor(): SemanticVersion {
        return this.copy(major = major + 1)
    }

    fun nextMinor(): SemanticVersion {
        return this.copy(minor = minor + 1)
    }

    fun nextPatch(): SemanticVersion {
        return this.copy(patch = patch + 1)
    }

    override fun compareTo(other: SemanticVersion): Int {
        if (major > other.major) return 1
        if (major < other.major) return -1
        if (minor > other.minor) return 1
        if (minor < other.minor) return -1
        if (patch > other.patch) return 1
        if (patch < other.patch) return -1

        if (preRelease != null && other.preRelease == null) return -1
        if (preRelease == null && other.preRelease != null) return 1
        if (preRelease == null && other.preRelease == null) return 0

        val preReleaseParts = preRelease!!.split(".")
        val otherPreReleaseParts = other.preRelease!!.split(".")
        val smallerSize = min(preReleaseParts.size, otherPreReleaseParts.size)
        for (i in 0 until smallerSize) {
            val part = preReleaseParts[i]
            val otherPart = otherPreReleaseParts[i]
            if (part == otherPart) continue

            val partIsNumeric = part.toLongOrNull() != null
            val otherPartIsNumeric = otherPart.toLongOrNull() != null

            return when {
                partIsNumeric && otherPartIsNumeric.not() -> -1
                partIsNumeric.not() && otherPartIsNumeric -> 1
                partIsNumeric.not() && otherPartIsNumeric.not() -> part.compareTo(otherPart)
                else -> try {
                    part.toLong().compareTo(otherPart.toLong())
                } catch (_: NumberFormatException) {
                    part.compareTo(otherPart)
                }
            }
        }
        return when {
            preReleaseParts.size > smallerSize -> 1
            otherPreReleaseParts.size > smallerSize -> -1
            else -> 0
        }
    }

    override fun toString(): String = buildString {
        append(major)
        append('.')
        append(minor)
        append('.')
        append(patch)
        if (preRelease != null) {
            append('-')
            append(preRelease)
        }
        if (buildMetadata != null) {
            append('+')
            append(buildMetadata)
        }
    }

    fun toCompactString(): String = buildString {
        append(major)
        append('.')
        append(minor)
        append('.')
        append(patch)
    }

    companion object {
        private val versionRegex =
            Regex("""^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-(\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(\d*[a-zA-Z-][0-9a-zA-Z-]*))*)?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$""")

        fun parse(versionString: String): SemanticVersion? {
            val matchResult = versionRegex.matchEntire(versionString)
            return matchResult?.let {
                val (major, minor, patch, preRelease, preReleaseComponent, buildMetadata) = matchResult.destructured
                return SemanticVersion(
                    major.toInt(),
                    minor.toInt(),
                    patch.toInt(),
                    preRelease.emptyToNull(),
                    buildMetadata.emptyToNull()
                )
            }
        }
    }
}

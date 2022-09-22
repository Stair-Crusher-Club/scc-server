package club.staircrusher.stdlib.annotation

import jakarta.inject.Named
import jakarta.inject.Singleton

/**
 * I can not be sure whether it does work or not, so if it does not work I
 * changed it to java or use spring annotation instead of jakarta.
 */
@Named
@Singleton
@Retention(AnnotationRetention.RUNTIME)
annotation class Component(
)

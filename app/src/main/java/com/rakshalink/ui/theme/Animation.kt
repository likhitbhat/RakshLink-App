package com.rakshalink.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

object AnimationSpecs {
    val QuickTransition = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
    val StandardTransition = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    val PulseAnimation = tween<Float>(durationMillis = 1200, easing = LinearEasing)
    val SplashDurationMs = 2200L
}

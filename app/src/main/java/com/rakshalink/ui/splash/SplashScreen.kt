package com.rakshalink.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlowIcon
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.CyanGlow
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }
    val ring1Radius = remember { Animatable(0f) }
    val ring2Radius = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1 & 2: Logo appears & scales in (0ms - 600ms)
        logoAlpha.animateTo(1f, tween(400))
        logoScale.animateTo(1.0f, tween(600, easing = FastOutSlowInEasing))

        // Step 3 & 4: First ring expands (600ms - 1200ms)
        ring1Radius.animateTo(140f, tween(600))

        // Step 5: Second ring expands (1000ms - 1600ms)
        ring2Radius.animateTo(220f, tween(600))

        // Step 6: Brand text fades in (1400ms - 2000ms)
        textAlpha.animateTo(1f, tween(600))

        delay(400L) // Total ~2.2s
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Rings Canvas
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = this.center
            if (ring1Radius.value > 0f) {
                drawCircle(
                    color = CyanAccent.copy(alpha = (1f - ring1Radius.value / 140f).coerceIn(0f, 0.6f)),
                    radius = ring1Radius.value.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            if (ring2Radius.value > 0f) {
                drawCircle(
                    color = PrimaryRed.copy(alpha = (1f - ring2Radius.value / 220f).coerceIn(0f, 0.4f)),
                    radius = ring2Radius.value.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center Logo & Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            ) {
                GlowIcon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "RakshaLink Logo",
                    iconTint = CyanAccent,
                    glowColor = CyanGlow,
                    size = 64.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "RakshaLink",
                    color = TextPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "IoT-ENABLED EMERGENCY PROTECTION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

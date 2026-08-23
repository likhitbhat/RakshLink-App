package com.rakshalink.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rakshalink.ui.theme.CardCornerRadius
import com.rakshalink.ui.theme.SurfaceDark

@Composable
fun SkeletonShimmer(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    shape: RoundedCornerShape = RoundedCornerShape(CardCornerRadius)
) {
    val transition = rememberInfiniteTransition(label = "SkeletonShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        SurfaceDark,
        Color(0xFF242B33),
        SurfaceDark
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, translateAnim - 200f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun SkeletonDashboardCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            SkeletonShimmer(height = 20.dp, shape = RoundedCornerShape(4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonShimmer(height = 48.dp, shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonShimmer(height = 16.dp, shape = RoundedCornerShape(4.dp))
        }
    }
}

package com.rakshalink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.CyanGlow

@Composable
fun GlowIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconTint: Color = CyanAccent,
    glowColor: Color = CyanGlow,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = CircleShape, spotColor = glowColor, ambientColor = glowColor)
            .clip(CircleShape)
            .background(glowColor)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(size)
        )
    }
}

package com.rakshalink.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardCornerRadius = 24.dp
val ButtonCornerRadius = 16.dp
val SheetCornerRadius = 28.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(CardCornerRadius),
    extraLarge = RoundedCornerShape(SheetCornerRadius)
)

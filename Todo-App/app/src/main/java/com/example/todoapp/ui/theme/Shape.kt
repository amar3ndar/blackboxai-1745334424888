package com.example.todoapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
)

// Custom shapes for specific components
val TodoCardShape = RoundedCornerShape(12.dp)
val QuadrantShape = RoundedCornerShape(16.dp)
val FloatingActionButtonShape = RoundedCornerShape(16.dp)
val SearchBarShape = RoundedCornerShape(24.dp)
val DialogShape = RoundedCornerShape(24.dp)

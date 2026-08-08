package org.senai.metrodoc.features.report.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import java.util.*

sealed interface DrawShape {
    val id: String

    data class Circle(
        override val id: String = UUID.randomUUID().toString(),
        val topLeft: Offset,
        val size: Size,
        val color: Color = Color.Red,
        val strokeWidth: Float = 2f,
    ) : DrawShape

    data class Rectangle(
        override val id: String = UUID.randomUUID().toString(),
        val topLeft: Offset,
        val size: Size,
        val color: Color = Color.Red,
        val strokeWidth: Float = 2f,
    ) : DrawShape

    data class Arrow(
        override val id: String = UUID.randomUUID().toString(),
        val start: Offset,
        val end: Offset,
        val color: Color = Color.Red,
        val strokeWidth: Float = 2f
    ) : DrawShape
}
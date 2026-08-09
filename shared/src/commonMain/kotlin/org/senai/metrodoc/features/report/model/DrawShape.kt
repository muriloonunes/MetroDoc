package org.senai.metrodoc.features.report.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import java.util.*

sealed interface DrawShape {
    val id: String

    enum class StrokeWidth(val value: Float) {
        THIN(2f),
        MEDIUM(4f),
        THICK(6f)
    }

    data class Circle(
        override val id: String = UUID.randomUUID().toString(),
        val topLeft: Offset,
        val size: Size,
        val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    data class Rectangle(
        override val id: String = UUID.randomUUID().toString(),
        val topLeft: Offset,
        val size: Size,
        val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    data class Arrow(
        override val id: String = UUID.randomUUID().toString(),
        val start: Offset,
        val end: Offset,
        val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    data class NumberBadge(
        override val id: String = UUID.randomUUID().toString(),
        val center: Offset,
        val number: Int,
        val color: Color = Color.Red,
        val textColor: Color = Color.White,
        val radius: Float = 24f,
    ) : DrawShape

    data class TextBox(
        override val id: String = UUID.randomUUID().toString(),
        val text: String,
        val topLeft: Offset,
        val size: Size,
        val color: Color = Color.White.copy(alpha = 0.8f),
        val textColor: Color = Color.Black,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape {
        data class TextEditState(
            val topLeft: Offset,
            val size: Size,
            val backgroundColor: Color,
            val textColor: Color,
            val strokeWidth: Float
        )
    }

    data class ClearGroup(
        override val id: String = UUID.randomUUID().toString(),
        val shapes: List<DrawShape>,
    ) : DrawShape
}
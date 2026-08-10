package org.senai.metrodoc.features.report.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import org.senai.metrodoc.common.util.ColorAsArgbSerializer
import org.senai.metrodoc.common.util.OffsetSerializer
import org.senai.metrodoc.common.util.SizeSerializer
import java.util.*

@Serializable
sealed interface DrawShape {
    val id: String

    @Serializable
    enum class StrokeWidth(val value: Float) {
        THIN(2f),
        MEDIUM(4f),
        THICK(6f)
    }

    @Serializable
    data class Circle(
        override val id: String = UUID.randomUUID().toString(),
        @Serializable(with = OffsetSerializer::class) val topLeft: Offset,
        @Serializable(with = SizeSerializer::class) val size: Size,
        @Serializable(with = ColorAsArgbSerializer::class) val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    @Serializable
    data class Rectangle(
        override val id: String = UUID.randomUUID().toString(),
        @Serializable(with = OffsetSerializer::class) val topLeft: Offset,
        @Serializable(with = SizeSerializer::class) val size: Size,
        @Serializable(with = ColorAsArgbSerializer::class) val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    @Serializable
    data class Arrow(
        override val id: String = UUID.randomUUID().toString(),
        @Serializable(with = OffsetSerializer::class) val start: Offset,
        @Serializable(with = OffsetSerializer::class) val end: Offset,
        @Serializable(with = ColorAsArgbSerializer::class) val color: Color = Color.Red,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape

    @Serializable
    data class NumberBadge(
        override val id: String = UUID.randomUUID().toString(),
        @Serializable(with = OffsetSerializer::class) val center: Offset,
        val number: Int,
        @Serializable(with = ColorAsArgbSerializer::class) val color: Color = Color.Red,
        @Serializable(with = ColorAsArgbSerializer::class) val textColor: Color = Color.White,
        val radius: Float = 24f,
    ) : DrawShape

    @Serializable
    data class TextBox(
        override val id: String = UUID.randomUUID().toString(),
        val text: String,
        @Serializable(with = OffsetSerializer::class) val topLeft: Offset,
        @Serializable(with = SizeSerializer::class) val size: Size,
        @Serializable(with = ColorAsArgbSerializer::class) val color: Color,
        @Serializable(with = ColorAsArgbSerializer::class) val textColor: Color = Color.Black,
        val strokeWidth: Float = StrokeWidth.MEDIUM.value,
    ) : DrawShape {
        data class TextEditState(
            val topLeft: Offset,
            val size: Size,
            val backgroundColor: Color,
            val textColor: Color,
            val strokeWidth: Float,
        )
    }

    @Serializable
    data class ClearGroup(
        override val id: String = UUID.randomUUID().toString(),
        val shapes: List<DrawShape>,
    ) : DrawShape

    @Serializable
    data class Erased(
        override val id: String = UUID.randomUUID().toString(),
        val shape: DrawShape,
        val index: Int
    ): DrawShape
}
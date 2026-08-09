package org.senai.metrodoc.common.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorAsArgbSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.toArgb())
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt())
}

// Salva o Offset como uma String "x,y"
object OffsetSerializer : KSerializer<Offset> {
    override val descriptor = PrimitiveSerialDescriptor("Offset", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Offset) = encoder.encodeString("${value.x},${value.y}")
    override fun deserialize(decoder: Decoder): Offset {
        val parts = decoder.decodeString().split(",")
        return Offset(parts[0].toFloat(), parts[1].toFloat())
    }
}

// Salva o Size como uma String "width,height"
object SizeSerializer : KSerializer<Size> {
    override val descriptor = PrimitiveSerialDescriptor("Size", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Size) = encoder.encodeString("${value.width},${value.height}")
    override fun deserialize(decoder: Decoder): Size {
        val parts = decoder.decodeString().split(",")
        return Size(parts[0].toFloat(), parts[1].toFloat())
    }
}
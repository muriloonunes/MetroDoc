package org.senai.metrodoc.features.report.model

import androidx.compose.ui.geometry.Size
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.senai.metrodoc.common.util.SizeSerializer
import java.util.*

@Serializable
@SerialName("Imagem")
data class Imagem(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val path: String,
    val legenda: String = "",
    val drawings: List<DrawShape> = emptyList(),
    @Serializable(with = SizeSerializer::class) val canvasSize: Size = Size.Zero,
)
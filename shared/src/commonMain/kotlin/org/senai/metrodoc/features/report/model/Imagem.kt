package org.senai.metrodoc.features.report.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("Imagem")
data class Imagem(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val path: String,
    val legenda: String = "",
)
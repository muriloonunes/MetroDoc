package org.senai.metrodoc.features.report.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface ReportBlock {
    val id: String

    @Serializable @SerialName("Texto")
    data class Texto(
        override val id: String = UUID.randomUUID().toString(),
        val conteudo: String = "",
        val emTopicos: Boolean = false,
    ) : ReportBlock

    @Serializable @SerialName("GaleriaImagem")
    data class GaleriaImagem(
        override val id: String = UUID.randomUUID().toString(),
        val imagens: List<Imagem> = emptyList(),
        val colunas: Int = 1,
        val legenda: String = "",
    ): ReportBlock {
        @Serializable @SerialName("Imagem")
        data class Imagem(
            val id: String = UUID.randomUUID().toString(),
            val nome: String = "",
            val path: String,
            val legenda: String = "",
        )
    }

    @Serializable @SerialName("QuebraPagina")
    data class QuebraPagina(
        override val id: String = UUID.randomUUID().toString(),
    ): ReportBlock
}
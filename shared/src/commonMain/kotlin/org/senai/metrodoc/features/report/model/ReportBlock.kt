package org.senai.metrodoc.features.report.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface ReportBlock {
    val id: String
    val isValid: Boolean

    @Serializable @SerialName("Texto")
    data class Texto(
        override val id: String = UUID.randomUUID().toString(),
        val conteudo: String = "",
        val emTopicos: Boolean = false,
    ) : ReportBlock {
        override val isValid: Boolean
            get() = conteudo.isNotBlank()
    }

    @Serializable @SerialName("GaleriaImagem")
    data class GaleriaImagem(
        override val id: String = UUID.randomUUID().toString(),
        val imagens: List<Imagem> = emptyList(),
        val colunas: Int = 1,
        val legenda: String = "",
    ): ReportBlock {
        override val isValid: Boolean
            get() = imagens.isNotEmpty() && imagens.all { it.path.isNotBlank() }
    }

    @Serializable @SerialName("QuebraPagina")
    data class QuebraPagina(
        override val id: String = UUID.randomUUID().toString(),
        override val isValid: Boolean = true
    ): ReportBlock
}
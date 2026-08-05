package org.senai.metrodoc.features.report.model

import java.util.*

interface ReportBlock {
    val id: String

    data class Texto(
        override val id: String = UUID.randomUUID().toString(),
        val conteudo: String = "",
        val emTopicos: Boolean = false,
    ) : ReportBlock

    data class GaleriaImagem(
        override val id: String = UUID.randomUUID().toString(),
        val imagens: List<Imagem> = emptyList(),
        val colunas: Int = 1,
        val legenda: String = "",
    ): ReportBlock {
        data class Imagem(
            val id: String = UUID.randomUUID().toString(),
            val path: String,
            val legenda: String = "",
        )
    }

    data class QuebraPagina(
        override val id: String = UUID.randomUUID().toString(),
    ): ReportBlock
}
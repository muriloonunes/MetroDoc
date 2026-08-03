package org.senai.metrodoc.features.report.model

import java.util.*

interface ReportSection {
    val id: String
    val titulo:String

    data class Introducao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Introdução",
        val relatorioTitulo: String = "",
        val textos: List<SubTexto> = listOf(
            SubTexto.Objetivo(),
            SubTexto.EscopoAnalise(),
            SubTexto.ReferenciaMedicao()
        )
    ) : ReportSection {
        sealed interface SubTexto {
            val id: String
            val titulo: String
            val texto: String

            data class Objetivo(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Objetivo",
                override val texto: String = ""
            ) : SubTexto

            data class EscopoAnalise(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Escopo da Análise",
                override val texto: String = ""
            ) : SubTexto

            data class ReferenciaMedicao(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Referência de Medição",
                override val texto: String = ""
            ) : SubTexto

            data class Customizado(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Novo Tópico",
                override val texto: String = ""
            ) : SubTexto
        }
    }

    data class Identificacao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Identificação da Medição",
        val reportData: ReportData
    ) : ReportSection

    data class ResultadosDimensionais(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Resultados Dimensionais",
        val measurements: List<MeasurementData>,
        val resumoDimensional: String = ""
    ) : ReportSection

    // 4. Exemplo de Seção Futura: Imagens e Anexos
    data class AnexoFotografico(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Anexo Fotográfico",
        val imagePaths: List<String> = emptyList(),
        val observacoes: String = ""
    ) : ReportSection
}
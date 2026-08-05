package org.senai.metrodoc.features.report.model

import java.util.*

interface ReportSection {
    val id: String
    val titulo: String
    val removivel: Boolean
    val movivel: Boolean

    data class Introducao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Introdução",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val relatorioTitulo: String = "",
        val textos: List<SubTexto> = listOf(
            SubTexto.Objetivo(),
            SubTexto.EscopoAnalise(),
            SubTexto.ReferenciaMedicao()
        ),
        val informacoesExtras: List<SubTexto> = emptyList(),
        val imagePath: String = "",
        val imagemLegenda: String = "",
        val observacoes: String = "",
    ) : ReportSection {
        sealed interface SubTexto {
            val id: String
            val titulo: String
            val texto: String

            data class Objetivo(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Objetivo",
                override val texto: String = "",
            ) : SubTexto

            data class EscopoAnalise(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Escopo da Análise",
                override val texto: String = "",
            ) : SubTexto

            data class ReferenciaMedicao(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Referência de Medição",
                override val texto: String = "",
            ) : SubTexto

            data class Customizado(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "",
                override val texto: String = "",
            ) : SubTexto
        }
    }

    data class Identificacao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Identificação da Medição",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val reportData: ReportData,
    ) : ReportSection

    data class ResultadosDimensionais(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Resultados Dimensionais",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val measurements: List<MeasurementData>,
        val resumoDimensional: String = "",
    ) : ReportSection

    data class Conclusao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Conclusão",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val conclusao: String = "",
    ) : ReportSection

    data class InterpretacaoResultados(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Interpretação dos Resultados",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val topicos: String = "",
    ) : ReportSection

    // 4. Exemplo de Seção Futura: Imagens e Anexos
    data class AnexoFotografico(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Anexo Fotográfico",
        override val removivel: Boolean = true,
        override val movivel: Boolean = true,
        val imagePaths: List<String> = emptyList(),
        val observacoes: String = "",
    ) : ReportSection
}
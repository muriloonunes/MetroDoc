package org.senai.metrodoc.features.report.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
sealed interface ReportSection {
    val id: String
    val titulo: String
    val removivel: Boolean
    val movivel: Boolean
    val isValid: Boolean

    @Serializable
    @SerialName("Introducao")
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
        val imagem: Imagem = Imagem(path = ""),
        val observacoes: String = "",
    ) : ReportSection {
        override val isValid: Boolean
            get() {
                return this.relatorioTitulo.isNotBlank() &&
                        imagem.path.isNotBlank() &&
                        imagem.legenda.isNotBlank() &&
                        this.textos.all { it.titulo.isNotBlank() && it.texto.isNotBlank() } &&
                        (informacoesExtras.isEmpty() || informacoesExtras.all { it.titulo.isNotBlank() && it.texto.isNotBlank() })
            }

        @Serializable
        sealed interface SubTexto {
            val id: String
            val titulo: String
            val texto: String

            @Serializable
            @SerialName("Objetivo")
            data class Objetivo(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Objetivo",
                override val texto: String = "",
            ) : SubTexto

            @Serializable
            @SerialName("EscopoAnalise")
            data class EscopoAnalise(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Escopo da Análise",
                override val texto: String = "",
            ) : SubTexto

            @Serializable
            @SerialName("ReferenciaMedicao")
            data class ReferenciaMedicao(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "Referência de Medição",
                override val texto: String = "",
            ) : SubTexto

            @Serializable
            @SerialName("Customizado")
            data class Customizado(
                override val id: String = UUID.randomUUID().toString(),
                override val titulo: String = "",
                override val texto: String = "",
            ) : SubTexto
        }
    }

    @Serializable
    @SerialName("Identificacao")
    data class Identificacao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Identificação da Medição",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        @Transient val reportData: ReportData = ReportData(),
    ) : ReportSection {
        override val isValid: Boolean
            get() = reportData.isValid
    }

    @Serializable
    @SerialName("ResultadosDimensionais")
    data class ResultadosDimensionais(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Resultados Dimensionais",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        @Transient val measurements: List<MeasurementData> = emptyList(),
        val resumoDimensional: String = "",
    ) : ReportSection {
        override val isValid: Boolean
            get() = measurements.all { it.isValid }
    }

    @Serializable
    @SerialName("Conclusao")
    data class Conclusao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Conclusão",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val conclusao: String = "",
    ) : ReportSection{
        override val isValid: Boolean
            get() = conclusao.isNotBlank()
    }

    @Serializable
    @SerialName("InterpretacaoResultados")
    data class InterpretacaoResultados(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Interpretação dos Resultados",
        override val removivel: Boolean = false,
        override val movivel: Boolean = true,
        val topicos: String = "",
    ) : ReportSection {
        override val isValid: Boolean
            get() = topicos.isNotBlank()
    }

    @Serializable
    @SerialName("Customizada")
    data class Customizada(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "",
        override val removivel: Boolean = true,
        override val movivel: Boolean = true,
        val blocos: List<ReportBlock> = emptyList(),
    ) : ReportSection{
        override val isValid: Boolean
            get() = blocos.all { it.isValid }
    }
}
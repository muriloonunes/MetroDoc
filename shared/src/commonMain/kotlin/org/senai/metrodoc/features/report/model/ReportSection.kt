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
    val errors: List<SectionError> get() = emptyList()

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
        override val errors: List<SectionError>
            get() {
                val list = mutableListOf<SectionError>()
                if (this.relatorioTitulo.isBlank()) {
                    list.add(
                        SectionError(
                            this.id,
                            this.titulo,
                            "Título do Relatório",
                            "O título do relatório é obrigatório."
                        )
                    )
                }
                if (imagem.path.isBlank()) {
                    list.add(SectionError(id, titulo, "Foto do Componente", "A foto do componente é obrigatória"))
                }
                if (imagem.path.isNotBlank() && imagem.legenda.isBlank()) {
                    list.add(SectionError(id, titulo, "Legenda da Foto", "A legenda da foto é obrigatória"))
                }
                textos.forEach { subTexto ->
                    if (subTexto.titulo.isBlank()) {
                        list.add(
                            SectionError(
                                id,
                                titulo,
                                "Título do texto: ${subTexto.titulo.ifBlank { "Sem nome" }}",
                                "Preencha o campo"
                            )
                        )
                    }
                    if (subTexto.texto.isBlank()) {
                        list.add(
                            SectionError(
                                id,
                                titulo,
                                "Conteúdo de ${subTexto.titulo.ifBlank { "Texto da Introdução" }}",
                                "Preencha o campo"
                            )
                        )
                    }
                }

                informacoesExtras.forEachIndexed { index, extra ->
                    if (extra.titulo.isBlank() || extra.texto.isBlank()) {
                        list.add(
                            SectionError(
                                id,
                                titulo,
                                "Informação Extra #${index + 1} incompleta",
                                "Preencha o campo"
                            )
                        )
                    }
                }
                return list
            }

        override val isValid: Boolean
            get() = errors.isEmpty()

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
    ) : ReportSection {
        override val isValid: Boolean
            get() = true
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
        override val errors: List<SectionError>
            get() {
                val list = mutableListOf<SectionError>()
                measurements.forEachIndexed { index, m ->
                    if (!m.isValid) {
                        val caracteristicaNome = m.nome.ifBlank { "Item #${index + 1}" }
                        list.add(
                            SectionError(
                                id,
                                titulo,
                                "Característica '$caracteristicaNome' possui campos em branco",
                                ""
                            )
                        )
                    }
                }
                return list
            }

        override val isValid: Boolean
            get() = errors.isEmpty()
    }

    @Serializable
    @SerialName("Conclusao")
    data class Conclusao(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "Conclusão",
        override val removivel: Boolean = false,
        override val movivel: Boolean = false,
        val conclusao: String = "",
    ) : ReportSection {
        override val errors: List<SectionError>
            get() = if (conclusao.isBlank()) {
                listOf(SectionError(id, titulo, "Texto da Conclusão"))
            } else emptyList()

        override val isValid: Boolean
            get() = errors.isEmpty()
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
        override val errors: List<SectionError>
            get() = if (topicos.isBlank()) {
                listOf(SectionError(id, titulo, "Tópicos de Interpretação"))
            } else emptyList()

        override val isValid: Boolean
            get() = errors.isEmpty()
    }

    @Serializable
    @SerialName("Customizada")
    data class Customizada(
        override val id: String = UUID.randomUUID().toString(),
        override val titulo: String = "",
        override val removivel: Boolean = true,
        override val movivel: Boolean = true,
        val blocos: List<ReportBlock> = emptyList(),
    ) : ReportSection {
        override val errors: List<SectionError>
            get() {
                val list = mutableListOf<SectionError>()
                val nomeSecao = titulo.ifBlank { "Seção Sem Título" }

                if (titulo.isBlank()) {
                    list.add(SectionError(id, nomeSecao, "Título da Seção"))
                }

                blocos.forEachIndexed { index, bloco ->
                    when (bloco) {
                        is ReportBlock.Texto -> {
                            if (bloco.conteudo.isBlank()) {
                                list.add(SectionError(id, nomeSecao, "Bloco de Texto #${index + 1} em branco"))
                            }
                        }
                        is ReportBlock.GaleriaImagem -> {
                            if (bloco.imagens.isEmpty()) {
                                list.add(SectionError(id, nomeSecao, "Galeria #${index + 1} não possui imagens"))
                            } else {
                                bloco.imagens.forEachIndexed { imgIndex, img ->
                                    if (img.path.isBlank()) {
                                        list.add(SectionError(id, nomeSecao, "Imagem #${imgIndex + 1} da Galeria #${index + 1} sem arquivo"))
                                    }
                                }
                            }
                        }
                        is ReportBlock.QuebraPagina -> { }
                    }
                }
                return list
            }

        override val isValid: Boolean
            get() = errors.isEmpty()
    }
}
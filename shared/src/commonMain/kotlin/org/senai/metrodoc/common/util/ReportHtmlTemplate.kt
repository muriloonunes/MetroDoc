package org.senai.metrodoc.common.util

import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.util.PdfRenderEngine

object ReportHtmlTemplate {
    private var paginaTemplate: String? = null


    private suspend fun loadTemplate(): String {
        if (paginaTemplate == null) {
            paginaTemplate = ResourceUtils.getResourceAsString("files/template/report_template.html")
//            paginaTemplate = Res.readBytes("files/template/report_template.html").decodeToString()
        }
        return paginaTemplate!!
    }

    suspend fun generateHtml(
        reportData: ReportData,
        secoes: List<ReportSection>,
        originalPdfPath: String,
        renderEngine: PdfRenderEngine,
    ): String {
        val template = loadTemplate()
        val titulo = secoes.filterIsInstance<ReportSection.Introducao>()
            .firstOrNull { it.relatorioTitulo.isNotBlank() }
            ?.relatorioTitulo.orEmpty()
        val htmlSecoes = buildSectionsHtml(
            reportData = reportData,
            secoes = secoes,
            originalPdfPath = originalPdfPath,
            renderEngine = renderEngine
        )

        val cssDataUri = ResourceUtils.getResourceAsBase64("files/template/report_style.css", "text/css")
        val logoSenai = ResourceUtils.getResourceAsBase64("files/logo_senai.png")
        val logoCem = ResourceUtils.getResourceAsBase64("files/logo_cem.png")

        return template
            .replace("{{CSS_PATH}}", cssDataUri)
            .replace("{{TITULO}}", titulo)
            .replace("{{LOGO_SENAI}}", logoSenai)
            .replace("{{LOGO_CEM}}", logoCem)
            .replace("{{CONTEUDO_SECOES}}", htmlSecoes)
    }


    private suspend fun buildSectionsHtml(
        reportData: ReportData,
        secoes: List<ReportSection>,
        originalPdfPath: String,
        renderEngine: PdfRenderEngine,
    ): String {
        val sb = StringBuilder()

        val templateIntroducao = ResourceUtils.getResourceAsString("files/template/sections/introducao.html")
        val templateSubTexto = ResourceUtils.getResourceAsString("files/template/sections/sub_texto.html")
        val templateIdentificacao = ResourceUtils.getResourceAsString("files/template/sections/identificacao.html")
        val templateResultadosDimensionais =
            ResourceUtils.getResourceAsString("files/template/sections/resultados_dimensionais.html")
        val templateConclusao = ResourceUtils.getResourceAsString("files/template/sections/conclusao.html")

        secoes.forEach { secao ->
            when (secao) {
                is ReportSection.Introducao -> {
                    val subTextosHtml = secao.textos.joinToString(separator = "\n") {
                        templateSubTexto
                            .replace("{{SUB_TITULO}}", it.titulo)
                            .replace("{{SUB_TEXTO}}", it.texto.toHtmlText())
                    }

                    val img: String? = null
                    val legendaImagem = "Peça avaliada em processo de medição na MMC"
                    val primeiraLinhaHtml = if (!img.isNullOrBlank()) {
                        """
                            <td colspan="2" style="width: 50%;">
                                $subTextosHtml
                            </td>
                            <td colspan="2" style="width: 50%;" class="image-container-cell">
                                <img src="$img" class="intro-img" alt="Foto do Componente"/>
                                <div class="img-caption">$legendaImagem</div>
                            </td>
                        """.trimIndent()
                    } else {
                        """
                            <td colspan="4">
                                $subTextosHtml
                            </td>
                        """.trimIndent()
                    }

                    val introducaoHtml = templateIntroducao
                        .replace("{{RELATORIO_TITULO}}", secao.relatorioTitulo)
                        .replace("{{COMPONENTE_NOME}}", reportData.componente.ifEmpty { "Peça sem Nome" })
                        .replace("{{CONTEUDO_PRIMEIRA_LINHA}}", primeiraLinhaHtml)
                        .replace("{{QTD_VALORES}}", reportData.qtdCaracteristicas.ifEmpty { "0" })
                        .replace(
                            "{{QTD_FORA}}", reportData.caracteristicas.count {
                                it.isForaTolerancia
                            }.toString()
                        )
                        .replace("{{NOME_MMC}}", reportData.maquina)
                        .replace("{{OBSERVACOES}}", secao.observacoes.toHtmlText())

                    sb.append(introducaoHtml)
                }

                is ReportSection.Identificacao -> {
                    val identificacaoHtml = templateIdentificacao
                        .replace("{{NOME_CLIENTE}}", reportData.cliente)
                        .replace("{{COMPONENTE_NOME}}", reportData.componente)
                        .replace("{{IDENTIFICACAO_CALYPSO}}", reportData.identificadorCalypso)
                        .replace("{{NOME_MMC}}", reportData.maquina)
                        .replace("{{NUMERO_MMC}}", reportData.numeroMaquina)
                        .replace("{{SOFTWARE}}", reportData.software)
                        .replace("{{OPERADOR}}", reportData.operador)
                        .replace("{{DATA_HORA_MEDIÇÃO}}", reportData.dataHora)
                        .replace("{{QTD_CARACTERISTICAS}}", reportData.qtdCaracteristicas)

                    sb.append(identificacaoHtml)
                }

                is ReportSection.ResultadosDimensionais -> {
                    val medicoesHtml = reportData.caracteristicas
                        .filter { it.incluidaRelatorio }
                        .joinToString(separator = "\n") { item ->
                            val statusText = if (item.isForaTolerancia) "Fora" else "Dentro"
                            val statusClass = if (item.isForaTolerancia) "status-fora" else "status-dentro"

                            """
                                <tr>
                                    <td>${item.nome}</td>
                                    <td>${item.valorMedido}</td>
                                    <td>${item.valorNominal}</td>
                                    <td>${item.tolSuperior}</td>
                                    <td>${item.tolInferior}</td>
                                    <td>${item.desvio}</td>
                                    <td class="$statusClass">$statusText</td>
                                </tr>
                            """.trimIndent()
                        }
                    val resultadosDimensionaisHtml = templateResultadosDimensionais
                        .replace("{{DADOS_MEDICOES}}", medicoesHtml)
                    sb.append(resultadosDimensionaisHtml)
                }

                is ReportSection.Conclusao -> {
                    val conclusaoHtml = templateConclusao
                        .replace("{{CONCLUSAO_INDICE}}", (secoes.size - 1).toString())
                        .replace("{{CONCLUSAO_TEXTO}}", secao.conclusao.toHtmlText())
                    sb.append(conclusaoHtml)
                }

                else -> {

                }
            }
        }
        val templateAnexoOrigem = ResourceUtils.getResourceAsString("files/template/sections/anexo_original.html")
        val totalPaginas = renderEngine.loadPdf(originalPdfPath)
        val paginas = StringBuilder()

        for (p in 0 until totalPaginas) {
            val base64Image = renderEngine.renderPageAsBase64(p) ?: continue
            val numeroPag = p + 1
            paginas.append(
                """
                <div class="anexo-origem-page">
                    <img src="$base64Image" class="anexo-origem-img" alt="Relatorio de Origem Pagina $numeroPag"/>
                    <div class="observacoes-introducao">
                    Relatório de origem - ZEISS CALYPSO, página $numeroPag de $totalPaginas.
                </div>
                </div>
            """.trimIndent()
            )
        }
        sb.append(templateAnexoOrigem
            .replace("{{ANEXO_INDICE}}", secoes.size.toString())
            .replace("{{PAGINAS_ORIGEM}}", paginas.toString())
        )

        return sb.toString()
    }

    private fun String.toHtmlText(): String {
        return this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>")
    }
}
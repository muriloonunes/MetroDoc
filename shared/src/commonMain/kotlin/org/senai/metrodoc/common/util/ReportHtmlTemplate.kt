package org.senai.metrodoc.common.util

import org.senai.metrodoc.features.report.model.ReportBlock
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection

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
        isPreview: Boolean = false,
    ): String {
        val template = loadTemplate()
        val titulo = secoes.filterIsInstance<ReportSection.Introducao>()
            .firstOrNull { it.relatorioTitulo.isNotBlank() }
            ?.relatorioTitulo.orEmpty()
        val htmlSecoes = buildSectionsHtml(
            reportData = reportData,
            secoes = secoes,
            originalPdfPath = originalPdfPath,
            renderEngine = renderEngine,
            isPreview = isPreview
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
        isPreview: Boolean,
    ): String {
        val sb = StringBuilder()

        val templateIntroducao = ResourceUtils.getResourceAsString("files/template/sections/introducao.html")
        val templateSubTexto = ResourceUtils.getResourceAsString("files/template/sections/sub_texto.html")
        val templateIdentificacao = ResourceUtils.getResourceAsString("files/template/sections/identificacao.html")
        val templateResultadosDimensionais =
            ResourceUtils.getResourceAsString("files/template/sections/resultados_dimensionais.html")
        val templateInterpretacaoResultados =
            ResourceUtils.getResourceAsString("files/template/sections/interpretacao.html")
        val templateConclusao = ResourceUtils.getResourceAsString("files/template/sections/conclusao.html")
        val templateCustomizada = ResourceUtils.getResourceAsString("files/template/sections/secao_customizada.html")

        secoes.forEach { secao ->
            when (secao) {
                is ReportSection.Introducao -> {
                    val subTextosHtml = secao.textos.joinToString(separator = "\n") {
                        templateSubTexto
                            .replace("{{SUB_TITULO}}", it.titulo)
                            .replace("{{SUB_TEXTO}}", it.texto.toHtmlText())
                    }

                    val imgSecao = secao.imagem
                    val imgBase64 =
                        if (imgSecao.path.isNotBlank()) {
                            if (imgSecao.drawings.isEmpty()) {
                                ResourceUtils.localFileToBase64(imgSecao.path)
                            } else {
                                ResourceUtils.localImageWithDrawingsToBase64(
                                    path = imgSecao.path,
                                    drawings = imgSecao.drawings,
                                    uiCanvasSize = imgSecao.canvasSize
                                )
                            }
                        } else null
                    val legendaImagem = imgSecao.legenda.toHtmlText()
                    val primeiraLinhaHtml = if (!imgBase64.isNullOrBlank()) {
                        """
                            <td colspan="2" style="width: 50%;">
                                $subTextosHtml
                            </td>
                            <td colspan="2" style="width: 50%; text-align: center; vertical-align: middle;" class="image-container-cell">
                                    <img src="$imgBase64" class="intro-img" alt="Foto do Componente"/>
                                <div class="img-caption" style="text-align: center; margin-top: 6px;">$legendaImagem</div>
                            </td>
                        """.trimIndent()
                    } else {
                        """
                            <td colspan="4">
                                $subTextosHtml
                            </td>
                        """.trimIndent()
                    }

                    val extras = if (secao.informacoesExtras.isNotEmpty()) {
                        secao.informacoesExtras
                            .filter { it.titulo.isNotBlank() || it.texto.isNotBlank() }
                            .chunked(4)
                            .joinToString(separator = "\n") { grupo ->
                                val totalItens = grupo.size

                                val colunasHtml = grupo.mapIndexed { index, item ->
                                    val colSpan = if (index == totalItens - 1 && totalItens < 4) {
                                        4 - (totalItens - 1) //se for o ultimo da linha e tiver sobrando, pega o colspan pra fechar 4 colunas
                                    } else {
                                        1
                                    }

                                    val styleWidth = when (totalItens) {
                                        1 -> "style=\"width: 100%;\""
                                        2 -> "style=\"width: 50%;\""
                                        3 -> "style=\"width: 33.3%;\""
                                        else -> "style=\"width: 25%;\""
                                    }

                                    """
                                    <td colspan="$colSpan" $styleWidth>
                                        <div class="metric-label">${item.titulo.uppercase()}</div>
                                        <div class="section-content" style="font-size: 10pt; font-weight: bold; color: #1c1b1b; margin-top: 4px; margin-bottom: 0;">
                                            ${item.texto.toHtmlText()}
                                        </div>
                                    </td>
                                    """.trimIndent()
                                }.joinToString(separator = "\n")

                                "<tr>\n$colunasHtml\n</tr>"
                            }
                    } else ""

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
                        .replace("{{LINHAS_EXTRAS}}", extras)
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
                    val resumoDimensionalHtml = if (secao.resumoDimensional.isNotBlank()) {
                        """
                            <span>
                                Resumo dimensional: ${secao.resumoDimensional.toHtmlText()}
                            </span>
                        """.trimIndent()
                    } else ""
                    val resultadosDimensionaisHtml = templateResultadosDimensionais
                        .replace("{{DADOS_MEDICOES}}", medicoesHtml)
                        .replace("{{RESUMO_DIMENSIONAL}}", resumoDimensionalHtml)
                    sb.append(resultadosDimensionaisHtml)
                }

                is ReportSection.InterpretacaoResultados -> {
                    val interpretacaoHtml = templateInterpretacaoResultados
                        .replace("{{INTERPRETACAO_INDICE}}", secoes.indexOf(secao).toString())
                        .replace("{{TOPICOS}}", secao.topicos.toHtmlListItem())

                    sb.append(interpretacaoHtml)
                }

                is ReportSection.Conclusao -> {
                    val conclusaoHtml = templateConclusao
                        .replace("{{CONCLUSAO_INDICE}}", secoes.indexOf(secao).toString())
                        .replace("{{CONCLUSAO_TEXTO}}", secao.conclusao.toHtmlText())
                    sb.append(conclusaoHtml)
                }

                is ReportSection.Customizada -> {
                    val blocosHtmlBuilder = StringBuilder()
                    secao.blocos.forEach { bloco ->
                        when (bloco) {
                            is ReportBlock.Texto -> {
                                if (bloco.conteudo.isNotBlank()) {
                                    if (bloco.emTopicos) {
                                        blocosHtmlBuilder.append(
                                            """
                                                <ul class="lista-topicos">
                                                    ${bloco.conteudo.toHtmlListItem()}
                                                </ul>
                                            """.trimIndent()
                                        )
                                    } else {
                                        blocosHtmlBuilder.append(
                                            """
                                                <div class="section-content">
                                                    ${bloco.conteudo.toHtmlText()}
                                                </div>
                                            """.trimIndent()
                                        )
                                    }
                                }
                            }

                            is ReportBlock.GaleriaImagem -> {
                                if (bloco.imagens.isNotEmpty()) {
                                    val colunas = bloco.colunas.coerceIn(1, 4)
                                    val larguraPorc = 100 / colunas
                                    val temLegendaGeral = bloco.legenda.isNotBlank()

                                    val linhasHtml = bloco.imagens.chunked(colunas)
                                        .joinToString(separator = "\n") { grupo ->
                                            val colunasHtml = grupo.mapIndexed { index, imagem ->
                                                val isUltimoDaLinha = index == grupo.size - 1
                                                val colSpan = if (isUltimoDaLinha && grupo.size < colunas) {
                                                    colunas - (grupo.size - 1)
                                                } else {
                                                    1
                                                }
                                                val srcImage = if (imagem.path.isNotBlank()) {
                                                    if (imagem.drawings.isNotEmpty()) {
                                                        ResourceUtils.localImageWithDrawingsToBase64(
                                                            path = imagem.path,
                                                            drawings = imagem.drawings,
                                                            uiCanvasSize = imagem.canvasSize
                                                        )
                                                    } else {
                                                        ResourceUtils.localFileToBase64(imagem.path)
                                                    }
                                                } else {
                                                    ""
                                                }
                                                val legendaIndivHtml =
                                                    if (!temLegendaGeral && imagem.legenda.isNotBlank()) {
                                                        "<div class=\"img-caption\">${imagem.legenda.toHtmlText()}</div>"
                                                    } else ""

                                                """
                                                    <td colspan="$colSpan" style="width: $larguraPorc%; text-align: center; vertical-align: middle; padding: 6px;">
                                                        <div style="text-align: center;">
                                                            <img src="$srcImage" class="intro-img" style="max-height: 180px;" alt="Foto"/>
                                                        </div>
                                                        $legendaIndivHtml
                                                    </td>
                                                """.trimIndent()
                                            }.joinToString("\n")
                                            "<tr>\n$colunasHtml\n</tr>"
                                        }
                                    val legendaGeralHtml = if (temLegendaGeral) {
                                        """
                                            <div class="img-caption" style="margin-top: 6px; font-weight: bold;">
                                                ${bloco.legenda.toHtmlText()}
                                            </div>
                                        """.trimIndent()
                                    } else ""
                                    blocosHtmlBuilder.append(
                                        """
                                            <div style="margin: 12px 0; text-align: center;">
                                                <table style="width: 100%; border-collapse: collapse; margin: 0 auto;">
                                                    $linhasHtml
                                                </table>
                                                $legendaGeralHtml
                                            </div>
                                        """.trimIndent()
                                    )
                                }
                            }

                            is ReportBlock.QuebraPagina -> {
                                blocosHtmlBuilder.append("<div style=\"page-break-after: always;\"></div>")
                            }
                        }
                    }
                    val secaoCustomizadaHtml = templateCustomizada
                        .replace("{{SECAO_INDICE}}", secoes.indexOf(secao).toString())
                        .replace("{{SECAO_TITULO}}", secao.titulo.uppercase())
                        .replace("{{CONTEUDO_BLOCOS}}", blocosHtmlBuilder.toString())
                    sb.append(secaoCustomizadaHtml)
                }
            }
        }
        val templateAnexoOrigem = ResourceUtils.getResourceAsString("files/template/sections/anexo_original.html")
        val paginas = StringBuilder()

        if (isPreview) {
            paginas.append(
                """
                <div class="anexo-origem-page" style="padding: 40px; border: 2px dashed #b0c4de; background-color: #f9fbfd; margin-top: 20px;">
                    <p style="font-weight: bold; color: #005A9C;">[Pré-visualização] Anexo do Relatório de Origem</p>
                    <p style="color: #666; font-size: 9pt;">As páginas renderizadas do PDF original serão anexadas aqui no relatório final emitido.</p>
                </div>
                """.trimIndent()
            )
        } else {
            if (originalPdfPath.isNotBlank()) {
                val totalPaginas = renderEngine.loadPdf(originalPdfPath)
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
            }
        }
        sb.append(
            templateAnexoOrigem
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

    private fun String.toHtmlListItem(): String {
        return this.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n") { topico ->
                "<li>${topico.toHtmlText()}</li>"
            }
    }
}
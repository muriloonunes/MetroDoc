package org.senai.metrodoc.common.util

import metrodoc.shared.generated.resources.Res
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection

object ReportHtmlTemplate {
    private var htmlTemplate: String? = null
        private var cssStyle: String? = null


    private suspend fun loadTemplate(): String {
        if (htmlTemplate == null) {
            htmlTemplate = Res.readBytes("files/template/report_template.html").decodeToString()
        }
        return htmlTemplate!!
    }

        private suspend fun loadCss(): String {
        if (cssStyle == null) {
            cssStyle = Res.readBytes("files/template/report_style.css").decodeToString()
        }
        return cssStyle!!
    }

    suspend fun generateHtml(
        reportData: ReportData,
        secoes: List<ReportSection>,
    ): String {
        val template = loadTemplate()
        val css = loadCss()
        val titulo = secoes.find {
            it is ReportSection.Introducao && it.relatorioTitulo.isNotBlank()
        }?.let { (it as ReportSection.Introducao).relatorioTitulo } ?: ""
        val htmlSecoes = buildSectionsHtml(reportData, secoes)
        // Obtendo a logo usando o Res.readBytes do Compose
        val logoBytes = Res.readBytes("files/logo_senai.png")
        val logoSenaiBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes)
        return template
            .replace("{{CSS}}", css)
            .replace("{{TITULO}}", titulo)
            .replace("{{LOGO_SENAI}}", logoSenaiBase64)
            .replace("{{CONTEUDO_SECOES}}", htmlSecoes)
    }


    // A função DEVE retornar String em vez de Unit
    private fun buildSectionsHtml(
        reportData: ReportData,
        secoes: List<ReportSection>,
    ): String {
        val sb = StringBuilder()

        secoes.forEach { secao ->
            when (secao) {
                is ReportSection.Introducao -> {
                    sb.append(
                        """
                        <div class="main-title">${secao.relatorioTitulo.ifEmpty { "RELATÓRIO TÉCNICO" }}</div>
                        <div class="component-name">${reportData.componente.ifEmpty { "Peça sem identificação" }}</div>
                        
                        <table class="grid-table">
                            <tr>
                                <td>
                    """.trimIndent()
                    )

                    secao.textos.forEach { sub ->
                        sb.append(
                            """
                            <div class="section-label">${sub.titulo}</div>
                            <div class="section-content">${sub.texto.ifEmpty { "-" }}</div>
                        """.trimIndent()
                        )
                    }

                    sb.append(
                        """
                                </td>
                            </tr>
                        </table>
                    """.trimIndent()
                    )
                }

                is ReportSection.Identificacao -> {
                    sb.append(
                        """
                        <table class="grid-table bg-light-blue">
                            <tr>
                                <td style="width: 33%;">
                                    <div class="metric-label">AMOSTRA</div>
                                    <div class="metric-value">1 peça</div>
                                </td>
                                <td style="width: 33%;">
                                    <div class="metric-label">VALORES AVALIADOS</div>
                                    <div class="metric-value">${reportData.qtdCaracteristicas.ifEmpty { "0" }}</div>
                                </td>
                                <td style="width: 33%;">
                                    <div class="metric-label">MMC</div>
                                    <div class="metric-value">${reportData.maquina.ifEmpty { "-" }}</div>
                                </td>
                            </tr>
                        </table>
                    """.trimIndent()
                    )
                }

                else -> {}
            }
        }

        return sb.toString()
    }
}
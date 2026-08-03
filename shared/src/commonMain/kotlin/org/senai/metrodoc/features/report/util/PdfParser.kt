package org.senai.metrodoc.features.report.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.senai.metrodoc.features.report.model.MeasurementData
import org.senai.metrodoc.features.report.model.ReportData
import java.io.File

class PdfParser {
    private object ZeissReportRegex {
        val NOME = Regex("^(?:Nome|Part name)\\s+(.+)", RegexOption.IGNORE_CASE)
        val NOME_MMC = Regex("^(?:Nome da MMC|Modelo MMC)\\s+(.+)", RegexOption.IGNORE_CASE)
        val NUM_MMC = Regex("^(?:Numero da MMC|Nº MMC)\\s+(.+)", RegexOption.IGNORE_CASE)
        val OPERADOR = Regex("^(?:Operador|Operator)\\s+(.+)", RegexOption.IGNORE_CASE)
        val DATA_HORA = Regex("^(?:Data/Hora|Time/Date)\\s+(.+)", RegexOption.IGNORE_CASE)
        val QTD_CARACT = Regex("^(?:Numero de medições|Number measured values)\\s+(.+)", RegexOption.IGNORE_CASE)
        val VERSAO = Regex("\\d+(\\.\\d+)+")

        val MEDICAO = Regex(
            "^([A-Za-z_.-][A-Za-z0-9_.-]*(?:\\s+[A-Za-z0-9_.-]+)*)\\s+(-?\\d+,\\d+)\\s*(mm|inch)?\\s+(-?\\d+,\\d+.*)\$",
            RegexOption.IGNORE_CASE
        )

        val LIXO = setOf(
            "corner", "max", "min", "pontos", "lc", "upr", "vmess",
            "raio", "page", "run", "last", "name", "tipo", "mtodo", "metodo"
        )
    }

    suspend fun parsePdf(path: String): ReportData =
        withContext(Dispatchers.IO) {
            val file = File(path)
            require(file.exists()) { "Arquivo não encontrado: $path" }

            Loader.loadPDF(file).use { document ->
                val stripper = PDFTextStripper()
                val pdfText = stripper.getText(document)
                parseText(pdfText)
            }
        }


    private fun parseText(pdfText: String): ReportData {
        val linhas = pdfText.lines()
        val totalLinhas = linhas.size

        var identificadorCalypso = ""
        var nomeMmC = ""
        var numeroMmc = ""
        var operador = ""
        var dataHora = ""
        var qtdCaracteristicas = ""
        var versaoCalypso = ""

        var unidadeAtual = "mm"
        val medicoes = mutableListOf<MeasurementData>()

        var ignorarLegendaGrafico = false

        for (i in linhas.indices) {
            val trimmed = linhas[i].trim()
            if (trimmed.isEmpty()) continue
//            println(trimmed)

            val lineLower = trimmed.lowercase()
            if (lineLower.startsWith("corner points") || lineLower.startsWith("corner")) {
                ignorarLegendaGrafico = true
                continue
            }
            if (ignorarLegendaGrafico) {
                if (lineLower.startsWith("min")) {
                    ignorarLegendaGrafico = false
                }
                continue
            }

            if (identificadorCalypso.isEmpty()) {
                ZeissReportRegex.NOME.find(trimmed)?.let { identificadorCalypso = it.groupValues[1].trim() }
            }
            if (nomeMmC.isEmpty()) {
                ZeissReportRegex.NOME_MMC.find(trimmed)?.let { nomeMmC = it.groupValues[1].trim() }
            }
            if (numeroMmc.isEmpty()) {
                ZeissReportRegex.NUM_MMC.find(trimmed)?.let { numeroMmc = it.groupValues[1].trim() }
            }
            if (operador.isEmpty()) {
                ZeissReportRegex.OPERADOR.find(trimmed)?.let { operador = it.groupValues[1].trim() }
            }
            if (dataHora.isEmpty()) {
                ZeissReportRegex.DATA_HORA.find(trimmed)?.let { dataHora = it.groupValues[1].trim() }
            }
            if (qtdCaracteristicas.isEmpty()) {
                ZeissReportRegex.QTD_CARACT.find(trimmed)?.let { qtdCaracteristicas = it.groupValues[1].trim() }
            }

            if (versaoCalypso.isEmpty()) {
                if (trimmed.equals("CALYPSO", ignoreCase = true) && i > 0) {
                    val linhaAnterior = linhas[i - 1].trim()
                    if (ZeissReportRegex.VERSAO.matches(linhaAnterior)) {
                        versaoCalypso = linhaAnterior
                        continue
                    }
                } else if (trimmed.equals("ZEISS CALYPSO", ignoreCase = true) && i < totalLinhas - 1) {
                    val linhaSeguinte = linhas[i + 1].trim()
                    if (ZeissReportRegex.VERSAO.matches(linhaSeguinte)) {
                        versaoCalypso = linhaSeguinte
                        continue
                    }
                }
            }

            val primeiraPalavra = trimmed.substringBefore(" ").lowercase()
            if (primeiraPalavra in ZeissReportRegex.LIXO) continue
            if (trimmed.startsWith("X") || trimmed.startsWith("Y") || trimmed.startsWith("Z")) continue

            val matchMedicao = ZeissReportRegex.MEDICAO.find(trimmed)
            if (matchMedicao != null) {
                val nome = matchMedicao.groupValues[1].trim()
                val measured = matchMedicao.groupValues[2].trim()
                val unidadeRaw = matchMedicao.groupValues[3].trim()
                val restoNumeros = matchMedicao.groupValues[4].trim()

                if (unidadeRaw.isNotEmpty()) {
                    unidadeAtual = unidadeRaw
                }

                val tokensNumericos = restoNumeros.split("\\s+".toRegex())

                val medicaoProcessada = processarTokensDeMedicao(
                    nome = nome,
                    measured = measured,
                    unidade = unidadeAtual,
                    tokens = tokensNumericos
                )

                medicoes.add(medicaoProcessada)
            }
        }
//        println("MEDICOES: \n $medicoes")
        val reportData = ReportData(
            identificadorCalypso = identificadorCalypso,
            maquina = nomeMmC,
            numeroMaquina = numeroMmc,
            operador = operador,
            dataHora = dataHora,
            qtdCaracteristicas = qtdCaracteristicas,
            software = "ZEISS CALYPSO $versaoCalypso",
            caracteristicas = medicoes
        )

        return reportData
    }

    private fun processarTokensDeMedicao(
        nome: String,
        measured: String,
        unidade: String,
        tokens: List<String>,
    ): MeasurementData {
        val nominal = tokens.getOrElse(0) { "-" }
        var tolSup = "-"
        var tolInf = "-"
        var desvio = "-"
        var isForaTolerancia = false

        when (tokens.size) {
            2 -> {
                desvio = tokens[1]
            }

            4 -> {
                tolSup = tokens[1]
                tolInf = tokens[2]
                desvio = tokens[3]
            }

            else -> if (tokens.size >= 5) {
                tolSup = tokens[1]
                tolInf = tokens[2]
                desvio = tokens[3]
                isForaTolerancia = true // Presença do desvio excedente final indica NOK
            }
        }
        return MeasurementData(
            nome = nome,
            valorMedido = measured,
            unidade = unidade,
            valorNominal = nominal,
            tolSuperior = tolSup,
            tolInferior = tolInf,
            desvio = desvio,
            isForaTolerancia = isForaTolerancia
        )
    }
}
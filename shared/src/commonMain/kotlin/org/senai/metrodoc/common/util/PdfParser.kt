package org.senai.metrodoc.common.util

import dev.nucleusframework.pdfium.PdfReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.senai.metrodoc.features.report.model.*
import java.io.File

class PdfParser {
    companion object {
        /**
         * Procura pela primeira página no pdfFile que contém o `searchTerm` fornecido.
         *
         * Esta função realiza uma busca de substring insensível a maiúsculas/minúsculas (case-insensitive)
         * sobre os blocos de texto (text runs) do layout de cada página.
         * Ela retorna o índice da página baseado em zero da primeira página onde qualquer bloco de texto
         * contém o `searchTerm`.
         *
         * @param reader PdfReaderState que fornece acesso às páginas, contagem de páginas e layouts de texto das páginas.
         * @param searchTerm O termo a ser pesquisado. A correspondência é insensível a maiúsculas/minúsculas.
         *                   Espaços em branco no início/fim são tratados como parte do termo de busca;
         *                   termos em branco retornam -1.
         * @return Índice de página baseado em zero contendo o termo de busca, ou -1 se o termo estiver em branco ou não for encontrado.
         *
         * @see <a href="https://github.com/NucleusFramework/ComposePdfReader#6-search-across-pages-and-highlight-hits">Documentação do ComposePdfReader - Busca</a>
         * */
        suspend fun getPageFromSearch(reader: PdfReaderState, searchTerm: String): Int {
            if (searchTerm.isBlank()) return -1
            for (page in 0 until reader.pageCount) {
                val layout = reader.pageTextLayout(page) ?: continue
                for (i in 0 until layout.rectCount) {
                    val run = layout.text(i)
                    if (run.contains(searchTerm, true)) {
                        return page
                    }
                }
            }
            return -1
        }
    }

    private object ZeissReportRegex {
        val NOME = Regex("^(?:Nome|Part name)\\s+(.+)", RegexOption.IGNORE_CASE)
        val NOME_MMC = Regex("^(?:Nome da MMC|Modelo MMC)\\s+(.+)", RegexOption.IGNORE_CASE)
        val NUM_MMC = Regex("^(?:Numero da MMC|Nº MMC)\\s+(.+)", RegexOption.IGNORE_CASE)
        val OPERADOR = Regex("^(?:Operador|Operator)\\s+(.+)", RegexOption.IGNORE_CASE)
        val DATA_HORA = Regex("^(?:Data/Hora|Time/Date)\\s+(.+)", RegexOption.IGNORE_CASE)
        val QTD_CARACT = Regex("^(?:Numero de medições|Number measured values)\\s+(.+)", RegexOption.IGNORE_CASE)
        val VERSAO = Regex("\\d+(\\.\\d+)+")

        val MEDICAO = Regex(
            "^([A-Za-z_.-][A-Za-z0-9_.-]*(?:\\s+[A-Za-z0-9_.-]+)*)\\s+(-?\\d+,\\d+)\\s*(mm|inch)?\\s+(-?\\d+,\\d+.*)$",
            RegexOption.IGNORE_CASE
        )

        val INSPECT_DATA_ROW = Regex(
            "^(.*?)\\s+([A-Za-z_#]+)\\s+([-+0-9.,\\s]+)$",
            RegexOption.IGNORE_CASE
        )

        val LIXO = setOf(
            "corner", "max", "min", "pontos", "lc", "upr", "vmess",
            "raio", "page", "run", "last", "name", "tipo", "mtodo", "metodo"
        )
    }

    private enum class PdfType { CALYPSO, INSPECT, UNKNOWN }

    suspend fun parsePdf(path: String): ReportData =
        withContext(Dispatchers.IO) {
            val file = File(path)
            require(file.exists()) { "Arquivo não encontrado: $path" }

            Loader.loadPDF(file).use { document ->
                val stripper = PDFTextStripper()
                val pdfText = stripper.getText(document)

                val pdfType = checkPdfType(pdfText)

                when (pdfType) {
                    PdfType.CALYPSO -> parseCalypsoPdf(pdfText)
                    PdfType.INSPECT -> parseInspectPdf(pdfText)
                    PdfType.UNKNOWN -> throw IllegalArgumentException("Tipo de PDF desconhecido ou não suportado.")
                }

            }
        }

    private fun checkPdfType(pdfText: String): PdfType {
        val amostra = pdfText.take(2000).lowercase()
        return when {
            amostra.contains("calypso") -> PdfType.CALYPSO
            amostra.contains("zeiss inspec") -> PdfType.INSPECT
            else -> PdfType.UNKNOWN
        }
    }

    suspend fun parsePdfsInBatch(
        files: List<Pair<String, String>>,
        chunkSize: Int = 5,
        onProgress: (processed: Int) -> Unit = {},
    ): List<PdfItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PdfItem>()
        var processedCount = 0

        for (chunk in files.chunked(chunkSize)) {
            val chunkItems = chunk.map { (path, name) ->
                async {
                    try {
                        val data = parsePdf(path)
                        val initialSections = listOf(
                            ReportSection.Introducao(),
                            ReportSection.Identificacao(),
                            ReportSection.ResultadosDimensionais(measurements = data.caracteristicas),
                            ReportSection.InterpretacaoResultados(),
                            ReportSection.Conclusao(),
                        )
                        PdfItem(
                            pdfPath = path,
                            pdfName = name,
                            reportData = data,
                            secoes = initialSections,
                            status = PdfItemStatus.PARSED
                        )
                    } catch (e: Exception) {
                        PdfItem(
                            pdfPath = path,
                            pdfName = name,
                            status = PdfItemStatus.ERROR,
                            errorMessage = e.message ?: "Erro ao ler o arquivo PDF."
                        )
                    }
                }
            }.awaitAll()

            results.addAll(chunkItems)
            processedCount += chunk.size
            onProgress(processedCount)
        }

        results
    }


    private fun parseCalypsoPdf(pdfText: String): ReportData {
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

                val medicaoProcessada = processarTokensCalypso(
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

    private fun parseInspectPdf(pdfText: String): ReportData {
        val linhas = pdfText.lines()
        val medicoes = mutableListOf<MeasurementData>()

        var isDentroTabela = false
        var softwareVersion = "ZEISS INSPECT"

        for (linha in linhas) {
            val trimmed = linha.trim()
            if (trimmed.isEmpty()) continue

            val lower = trimmed.lowercase()

            if (lower.startsWith("generated with zeiss inspec")) {
                softwareVersion = trimmed.substringAfter("with ").replace("INSPEC T", "INSPECT")
                continue
            }

            if (lower.startsWith("element datum property") || lower.startsWith("nominal atual")) {
                isDentroTabela = true
                continue
            }

            if (lower.startsWith("alinhamento original") || lower.startsWith("unidade de comprimento")) {
                isDentroTabela = false
                continue
            }

            if (isDentroTabela) {
                val match = ZeissReportRegex.INSPECT_DATA_ROW.find(trimmed)
                if (match != null) {
                    val nomeElemento = match.groupValues[1].trim() // Ex: Defeito do volume 1.Vp.147
                    val propriedade = match.groupValues[2].trim()  // Ex: Vp
                    val numerosBrutos = match.groupValues[3].trim() // Ex: +44.82

                    val tokensNumericos = numerosBrutos.split("\\s+".toRegex())

                    // Adaptador para normalizar os dados do Inspect no seu MeasurementData
                    val medicao = processarTokensInspect(nomeElemento, propriedade, tokensNumericos)
                    medicoes.add(medicao)
                }
            }
        }
        return ReportData(
            identificadorCalypso = "Relatório de Inspeção 3D",
            maquina = "",
            numeroMaquina = "-",
            operador = "Não especificado",
            dataHora = "-",
            qtdCaracteristicas = medicoes.size.toString(),
            software = softwareVersion,
            caracteristicas = medicoes
        )
    }

    private fun processarTokensCalypso(
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

    private fun processarTokensInspect(
        nome: String,
        propriedade: String,
        tokens: List<String>,
    ): MeasurementData {
        var nominal = "-"
        var atual = "-"
        var desvio = "-"

        val nomeFormatado = "$nome ($propriedade)"

        when (tokens.size) {
            1 -> {
                atual = tokens[0]
            }

            2 -> {
                nominal = tokens[0]
                atual = tokens[1]
            }

            else -> if (tokens.size >= 3) {
                nominal = tokens[0]
                atual = tokens[1]
                desvio = tokens[2]
            }
        }

        return MeasurementData(
            nome = nomeFormatado,
            valorMedido = atual,
            unidade = "mm/mm³",
            valorNominal = nominal,
            tolSuperior = "-",
            tolInferior = "-",
            desvio = desvio,
            isForaTolerancia = false // Exigiria checar a coluna "Out" se ela vier preenchida
        )
    }
}
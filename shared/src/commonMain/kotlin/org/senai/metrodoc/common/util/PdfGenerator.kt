package org.senai.metrodoc.common.util

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import java.io.ByteArrayOutputStream

class PdfGenerator {
    suspend fun generatePdfBytes(
        reportData: ReportData,
        secoes: List<ReportSection>
    ): ByteArray = withContext(Dispatchers.IO) {
        val html = ReportHtmlTemplate.generateHtml(reportData, secoes)
        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()
        builder.useFastMode()
        builder.withHtmlContent(html, null)
        builder.toStream(os)
        builder.run()
        os.toByteArray()
    }
}
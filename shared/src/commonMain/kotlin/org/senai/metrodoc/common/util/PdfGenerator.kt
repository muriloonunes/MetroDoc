package org.senai.metrodoc.common.util

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.util.PdfRenderEngine
import java.io.ByteArrayOutputStream

class PdfGenerator {
    suspend fun generatePdfBytes(
        reportData: ReportData,
        secoes: List<ReportSection>,
        originalPdfPath: String,
        renderEngine: PdfRenderEngine,
    ): ByteArray = withContext(Dispatchers.IO) {
        val html = ReportHtmlTemplate.generateHtml(reportData, secoes, originalPdfPath, renderEngine)
        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()

        builder.withHtmlContent(html, null)
        builder.toStream(os)
        builder.run()
        os.toByteArray()
    }

    suspend fun generatePreviewPdfBytes(
        reportData: ReportData,
        secoes: List<ReportSection>,
        renderEngine: PdfRenderEngine,
    ): ByteArray = withContext(Dispatchers.IO) {
        val html = ReportHtmlTemplate.generateHtml(
            reportData = reportData,
            secoes = secoes,
            originalPdfPath = "",
            renderEngine = renderEngine,
            isPreview = true
        )
        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()

        builder.withHtmlContent(html, null)
        builder.toStream(os)
        builder.run()
        os.toByteArray()
    }
}
package org.senai.metrodoc.common.util

import androidx.compose.runtime.snapshotFlow
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.File

class PdfGenerator(
    val renderEngine: PdfRenderEngine
) {
    companion object {
        fun CoroutineScope.savePdf(
            file: PlatformFile?,
            getpdfBytes: () -> ByteArray?,
            onEnsureBytesGenerated: () -> Unit,
            onSuccess: () -> Unit = {},
            onCancel: () -> Unit = {}
        ) {
            if (file == null) {
                onCancel()
                return
            }

            this.launch {
                if (getpdfBytes() == null) {
                    onEnsureBytesGenerated()
                }

                val bytes = snapshotFlow { getpdfBytes() }
                    .filterNotNull()
                    .first()

                file.write(bytes)

                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file.file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                onSuccess()
            }
        }

        fun CoroutineScope.savePdfsBatch(
            directory: PlatformFile?,
            getPdfBytesList: () -> List<Pair<String, ByteArray>>?,
            onEnsureBytesGenerated: () -> Unit,
            onSuccess: () -> Unit = {},
            onCancel: () -> Unit = {}
        ) {
            if (directory == null) {
                onCancel()
                return
            }
            this.launch {
                if (getPdfBytesList() == null) {
                    onEnsureBytesGenerated()
                }

                val filesList = snapshotFlow { getPdfBytesList() }
                    .filterNotNull()
                    .first()

                withContext(Dispatchers.IO) {
                    val outDir = File(directory.path)
                    if (!outDir.exists()) outDir.mkdirs()

                    filesList.forEach { (name, bytes) ->
                        val safeName = name.ifBlank { "relatorio" }
                        val targetFile = File(outDir, "$safeName.pdf")
                        targetFile.writeBytes(bytes)
                    }

                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(outDir)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                onSuccess()
            }
        }
    }

    suspend fun generatePdfBytes(
        reportData: ReportData,
        secoes: List<ReportSection>,
        originalPdfPath: String,
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
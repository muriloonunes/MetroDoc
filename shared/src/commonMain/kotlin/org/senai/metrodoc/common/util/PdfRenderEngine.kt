package org.senai.metrodoc.common.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PdfRenderEngine {
    private var renderer: PDFRenderer? = null
    private var document: PDDocument? = null

    suspend fun loadPdf(path: String): Int = withContext(Dispatchers.IO) {
        close() //fecho o anterior se ele existir

        val file = File(path)
        require(file.exists()) { "Arquivo não encontrado: $path" }

        val doc = Loader.loadPDF(file)
        document = doc
        renderer = PDFRenderer(doc)
        doc.numberOfPages
    }

    suspend fun renderPage(page: Int, dpi: Float = 150f): ImageBitmap? = withContext(Dispatchers.IO) {
        val doc = document ?: return@withContext null
        val currentRenderer = renderer ?: return@withContext null

        if (page !in 0 until doc.numberOfPages) return@withContext null

        runCatching {
            currentRenderer.renderImageWithDPI(page, dpi).toComposeImageBitmap()
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

    suspend fun renderPageAsBase64(pageIndex: Int, dpi: Float = 200f): String? = withContext(Dispatchers.IO) {
        val doc = document ?: return@withContext null
        val currentRenderer = renderer ?: return@withContext null
        if (pageIndex !in 0 until doc.numberOfPages) return@withContext null

        runCatching {
            val bufferedImage = currentRenderer.renderImageWithDPI(pageIndex, dpi)
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "png", outputStream)
            val imageBytes = outputStream.toByteArray()
            "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes)
        }.getOrNull()
    }

    fun close() {
        runCatching { document?.close() }
        renderer = null
        document = null
    }
}
package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.senai.metrodoc.features.report.presentation.ui.PDFViewer

@Composable
fun PdfPreviewer(
    previewData: ByteArray?,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            previewData != null -> {
                val previewId = "preview_${previewData.contentHashCode()}"
                PDFViewer(
                    pdfPath = previewId,
                    cachedBytes = previewData
                )
            }

            isGenerating -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center).size(32.dp),
                )
            }

            else -> {
                Text(
                    text = "Aguardando dados para gerar visualização",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        if (previewData != null && isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

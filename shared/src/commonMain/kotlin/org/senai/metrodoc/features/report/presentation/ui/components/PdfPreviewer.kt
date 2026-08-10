package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.delay
import org.senai.metrodoc.common.util.PdfParser
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ui.PdfViewer
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PdfPreviewer(
    previewData: ByteArray?,
    isGenerating: Boolean,
    secoes: List<ReportSection>,
    secaoAtivaId: String?,
    modifier: Modifier = Modifier,
) {
    val secaoAtiva = secoes.find { it.id == secaoAtivaId }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            previewData != null -> {
                val reader = rememberPdfReaderState()
                val previewId = "preview_${previewData.contentHashCode()}"
                val targetPage by produceState(
                    initialValue = 0,
                    key1 = secaoAtivaId,
                ) {
                    value = when (secaoAtiva) {
                        is ReportSection.Introducao -> 0
                        is ReportSection.Identificacao -> 1
                        else -> {
                            while (reader.pageCount == 0) {
                                delay(50.milliseconds)
                            }
                            PdfParser.getPageFromSearch(
                                reader = reader,
                                searchTerm = secaoAtiva?.titulo ?: "",
                            )
                        }
                    }
                }

                PdfViewer(
                    pdfPath = previewId,
                    cachedBytes = previewData,
                    targetPage = targetPage,
                    reader = reader,
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
                    .size(32.dp)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = CircleShape)
                    .padding(4.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

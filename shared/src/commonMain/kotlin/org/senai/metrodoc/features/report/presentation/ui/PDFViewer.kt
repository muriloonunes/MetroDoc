package org.senai.metrodoc.features.report.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.rememberPdfReaderState
import java.io.File

@Composable
fun PDFViewer(
    pdfPath: String,
    modifier: Modifier = Modifier,
) {
    val reader = rememberPdfReaderState()
    LaunchedEffect(pdfPath) {
        println("PDF path: $pdfPath")
        if (pdfPath.isNotBlank()) {
            val file = File(pdfPath)
            if (file.exists() && file.isFile) {
                reader.open(file.readBytes())
            } else {
                println("Arquivo PDF não encontrado no caminho: $pdfPath")
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            reader.isLoading -> CircularProgressIndicator()
            reader.pageCount > 0 -> {
                val listState = rememberLazyListState()
                val pages = (0 until reader.pageCount).toList()
                LazyColumn(
                    modifier = modifier,
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(pages, key = { it }) { pageIndex ->
                        PdfPage(
                            state = reader,
                            pageIndex = pageIndex,
                            modifier = Modifier.padding(horizontal = 0.dp),
                            selectableText = true,
                            linksEnabled = false,
                        )
                    }
                }
            }
        }
    }
}
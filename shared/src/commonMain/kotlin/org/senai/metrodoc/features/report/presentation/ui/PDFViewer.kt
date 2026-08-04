package org.senai.metrodoc.features.report.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PDFViewer(
    pdfPath: String,
    cachedBytes: ByteArray?,
    modifier: Modifier = Modifier,
    onBytesLoaded: (String, ByteArray) -> Unit = { _, _ -> }
) {
    val reader = rememberPdfReaderState()
    var loadedPath by remember { mutableStateOf("") }

    LaunchedEffect(pdfPath, cachedBytes) {
        if (pdfPath.isBlank() || loadedPath == pdfPath) return@LaunchedEffect
        if (cachedBytes != null) {
            reader.open(cachedBytes)
            loadedPath = pdfPath
        } else {
            withContext(Dispatchers.IO) {
                val file = File(pdfPath)
                if (file.exists() && file.isFile) {
                    val bytes = file.readBytes()
                    withContext(Dispatchers.Main) {
                        reader.open(bytes)
                        onBytesLoaded(pdfPath, bytes)
                    }
                } else {
                    println("Arquivo PDF não encontrado no caminho: $pdfPath")
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            reader.isLoading -> CircularProgressIndicator()
            reader.pageCount > 0 -> {
                val listState = rememberLazyListState()
                val pages = (0 until reader.pageCount).toList()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(pages, key = { pageIndex -> "${loadedPath}_$pageIndex" }) { pageIndex ->
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
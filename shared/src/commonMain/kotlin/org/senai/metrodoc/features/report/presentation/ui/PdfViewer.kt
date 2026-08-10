package org.senai.metrodoc.features.report.presentation.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.senai.metrodoc.common.theme.metroDocDefaultScrollbarStyle
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PdfViewer(
    pdfPath: String,
    cachedBytes: ByteArray?,
    targetPage: Int? = null,
    modifier: Modifier = Modifier,
    reader: PdfReaderState = rememberPdfReaderState(),
    onBytesLoaded: (String, ByteArray) -> Unit = { _, _ -> },
) {
    var loadedPath by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(targetPage, reader.pageCount) {
        if (targetPage != null && targetPage >= 0 && reader.pageCount > 0) {
            delay(50.milliseconds)
            listState.animateScrollToItem(targetPage)
        }
    }

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
                val pages = (0 until reader.pageCount).toList()
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 12.dp),
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
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    style = metroDocDefaultScrollbarStyle(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
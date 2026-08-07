package org.senai.metrodoc.features.report.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.senai.metrodoc.common.ui.MetroDocLoadingDialog
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.presentation.ReportCreatorEffect
import org.senai.metrodoc.features.report.presentation.ReportCreatorIntent
import org.senai.metrodoc.features.report.presentation.ReportCreatorState
import org.senai.metrodoc.features.report.presentation.ui.components.*
import java.awt.Cursor

enum class RightPanelTab {
    PREVIEW,
    PDF_ORIGINAL
}

@Composable
fun ReportCreatorScreen(
    state: ReportCreatorState,
    effect: Flow<ReportCreatorEffect>,
    onIntent: (ReportCreatorIntent) -> Unit,
    onBack: () -> Unit,
) {
    val rootFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    val scope = rememberCoroutineScope()
    var pdfBytesToSave by remember { mutableStateOf<ByteArray?>(null) }
    val saverLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        if (file != null) {
            scope.launch {
                if (pdfBytesToSave == null) {
                    onIntent(ReportCreatorIntent.OnGeneratePdf(file.path))
                }
                val bytes = snapshotFlow { pdfBytesToSave }
                    .filterNotNull()
                    .first()

                file.write(bytes)

                pdfBytesToSave = null
            }
        }
    }

    LaunchedEffect(Unit) {
        effect.collect { effect ->
            when (effect) {
                is ReportCreatorEffect.NavigateBack -> {
                    onBack()
                }

                is ReportCreatorEffect.OnPdfGenerated -> {
                    pdfBytesToSave = effect.bytes
                }
            }
        }
    }

    val minSideBarWidth = 200.dp
    val maxSideBarWidth = 400.dp
    var sidebarWidth by remember { mutableStateOf(maxSideBarWidth) }
    var editorPanelRatio by remember { mutableFloatStateOf(0.5f) }

    val currentSection = state.secoes.find { it.id == state.secaoAtivaId }

    if (state.showBackDialog) {
        ConfirmBackDialog(
            onConfirm = { onIntent(ReportCreatorIntent.OnBackConfirmed) },
            onDismiss = { onIntent(ReportCreatorIntent.OnBackDismissed) }
        )
    }

    if (state.isGeneratingPdf) {
        MetroDocLoadingDialog(
            loadingMessage = "Gerando PDF",
            isCancelable = true,
            onCancelLoading = {
                onIntent(ReportCreatorIntent.OnCancelGeneration)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape
            }
    ) {
        TopToolbar(
            title = state.reportName,
            onUpdateTitle = {
                onIntent(ReportCreatorIntent.OnReportNameChanged(it))
            },
            onBackClick = { onIntent(ReportCreatorIntent.OnBackClicked) },
            onSave = { onIntent(ReportCreatorIntent.OnSaveProject) },
            onEmitReportClick = {
                saverLauncher.launch(
                    suggestedName = "${state.pdfName}_Relatorio.pdf",
                    defaultExtension = "pdf",
                    allowedExtensions = setOf("pdf")
                )
            },
            onFocusRoot = { rootFocusRequester.requestFocus() }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = maxWidth
            Row(modifier = Modifier.fillMaxSize()) {
                SectionSidebar(
                    secoes = state.secoes,
                    selectedId = state.secaoAtivaId,
                    onSelectSection = { onIntent(ReportCreatorIntent.OnSectionChange(it)) },
                    onIntent = onIntent,
                    onFocusRoot = { rootFocusRequester.requestFocus() },
                    modifier = Modifier.width(sidebarWidth).fillMaxHeight()
                )
                ResizingDivider { delta ->
                    sidebarWidth = (sidebarWidth + delta.dp).coerceIn(minSideBarWidth, maxSideBarWidth)
                }

                val remainingWidth = totalWidth - sidebarWidth - 12.dp

                Row(modifier = Modifier.width(remainingWidth).fillMaxHeight()) {
                    Box(
                        modifier = Modifier
                            .width(remainingWidth * editorPanelRatio)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        if (currentSection != null) {
                            SectionEditorPanel(
                                section = currentSection,
                                reportData = state.currentReport ?: ReportData(),
                                onIntent = onIntent,
                                onFocusRoot = { rootFocusRequester.requestFocus() }
                            )
                        } else {
                            Text(
                                text = "Nenhuma seção selecionada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    ResizingDivider(
                        onDrag = { delta ->
                            val deltaRatio = delta / remainingWidth.value
                            editorPanelRatio = (editorPanelRatio + deltaRatio).coerceIn(0.3f, 0.7f)
                        }
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        SecondaryTabRow(
                            selectedTabIndex = state.abaDireitaAtiva.ordinal,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = state.abaDireitaAtiva == RightPanelTab.PREVIEW,
                                onClick = { onIntent(ReportCreatorIntent.OnTabChange(RightPanelTab.PREVIEW)) },
                                text = { Text("Preview") }
                            )
                            Tab(
                                selected = state.abaDireitaAtiva == RightPanelTab.PDF_ORIGINAL,
                                onClick = { onIntent(ReportCreatorIntent.OnTabChange(RightPanelTab.PDF_ORIGINAL)) },
                                text = { Text("PDF Original") }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            when (state.abaDireitaAtiva) {
                                RightPanelTab.PREVIEW -> {
                                    PdfPreviewer(
                                        previewData = state.previewPdfBytes,
                                        isGenerating = state.isGeneratingPreview,
                                        secoes = state.secoes,
                                        secaoAtivaId = state.secaoAtivaId,
                                    )
                                }
                                RightPanelTab.PDF_ORIGINAL -> {
                                    PDFViewer(
                                        pdfPath = state.pdfPath,
                                        cachedBytes = state.pdf[state.pdfPath],
                                        onBytesLoaded = { path, bytes ->
                                            onIntent(ReportCreatorIntent.OnPdfLoaded(path, bytes))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResizingDivider(
    onDrag: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(6.dp)
            .fillMaxHeight()
            .background(Color.Transparent)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta -> onDrag(delta) }
            )
    ) {
        VerticalDivider(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}
package org.senai.metrodoc.features.report.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.Flow
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocLoadingDialog
import org.senai.metrodoc.common.util.PdfGenerator.Companion.savePdf
import org.senai.metrodoc.common.util.PdfGenerator.Companion.savePdfsBatch
import org.senai.metrodoc.features.report.model.Imagem
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.SavedState
import org.senai.metrodoc.features.report.presentation.ReportCreatorEffect
import org.senai.metrodoc.features.report.presentation.ReportCreatorIntent
import org.senai.metrodoc.features.report.presentation.ReportCreatorState
import org.senai.metrodoc.features.report.presentation.ui.components.*
import java.awt.Cursor

enum class RightPanelTab(
    val text: String,
    val res: DrawableResource,
) {
    PREVIEW("Preview", Res.drawable.preview),
    PDF_ORIGINAL("PDF Original", Res.drawable.original_pdf),
    VERSOES("Versões", Res.drawable.change_history),
    ERROS("Erros", Res.drawable.warning)
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
    var batchPdfBytesToSave by remember { mutableStateOf<List<Pair<String, ByteArray>>?>(null) }

    val saverLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = {},
        onResult = { file ->
            scope.savePdf(
                file = file,
                getpdfBytes = { pdfBytesToSave },
                onEnsureBytesGenerated = {
                    file?.path?.let { path ->
                        onIntent(ReportCreatorIntent.OnGeneratePdf(path))
                    }
                },
                onSuccess = {
                    pdfBytesToSave = null
                    onIntent(ReportCreatorIntent.OnSaveProject)
                }
            )
        }
    )

    val batchSaverLauncher = rememberDirectoryPickerLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = {},
        onResult = { directory ->
            scope.savePdfsBatch(
                directory = directory,
                getPdfBytesList = { batchPdfBytesToSave },
                onEnsureBytesGenerated = {
                    onIntent(ReportCreatorIntent.OnGenerateAllPdfs)
                },
                onSuccess = {
                    batchPdfBytesToSave = null
                    onIntent(ReportCreatorIntent.OnSaveProject)
                }
            )
        }
    )

    LaunchedEffect(Unit) {
        effect.collect { effect ->
            when (effect) {
                is ReportCreatorEffect.NavigateBack -> {
                    onBack()
                }

                is ReportCreatorEffect.OnPdfGenerated -> {
                    pdfBytesToSave = effect.bytes
                }

                is ReportCreatorEffect.OnBatchPdfsGenerated -> {
                    batchPdfBytesToSave = effect.files
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
            onDismiss = { onIntent(ReportCreatorIntent.OnBackDismissed) },
            onSave = { onIntent(ReportCreatorIntent.OnSaveProject) }
        )
    }

    if (state.isGeneratingPdf) {
        val loadingMessage = if (state.pdfItems.size > 1) "Gerando PDFs" else "Gerando PDF"
        val supportingMessage = if (state.pdfItems.size > 1) {
            "${state.processedPdfCount} de ${state.pdfItems.size} PDFs gerados"
        } else null
        val progress = if (state.pdfItems.size > 1) state.processedPdfCount.toFloat() / state.pdfItems.size else null
        MetroDocLoadingDialog(
            loadingMessage = loadingMessage,
            supportingMessage = supportingMessage,
            progress = progress,
            isCancelable = true,
            onCancelLoading = {
                onIntent(ReportCreatorIntent.OnCancelGeneration)
            }
        )
    }

    if (state.showEditDialog) {
        EditImageDialog(
            imagePath = state.editingImage?.path,
            initialDrawings = state.editingImage?.drawings ?: emptyList(),
            onDismissRequest = { onIntent(ReportCreatorIntent.OnEditImageDismissed) },
            onConfirmEdit = { drawings, size ->
                val updatedImage = state.editingImage?.copy(drawings = drawings, canvasSize = size) ?: Imagem(path = "")
                onIntent(ReportCreatorIntent.OnEditImageConfirmed(updatedImage))
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
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if ((keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.key == Key.S) {
                        if (state.reportSaveState == SavedState.Unsaved) {
                            onIntent(ReportCreatorIntent.OnSaveProject)
                        }
                        return@onKeyEvent true
                    }
                    if (keyEvent.key == Key.Escape) {
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        TopToolbar(
            title = state.reportName,
            exportEnabled = state.canExport,
            exportActiveEnabled = state.canExportActive,
            isMultiPdf = state.pdfItems.size > 1,
            savedState = state.reportSaveState,
            onUpdateTitle = {
                onIntent(ReportCreatorIntent.OnReportNameChanged(it))
            },
            onBackClick = { onIntent(ReportCreatorIntent.OnBackClicked) },
            onSave = { onIntent(ReportCreatorIntent.OnSaveProject) },
            onEmitReportClick = {
                val currentName = state.activePdfItem?.pdfName?.ifBlank { state.reportName } ?: state.reportName
                saverLauncher.launch(
                    suggestedName = "$currentName.pdf",
                    defaultExtension = "pdf",
                    allowedExtensions = setOf("pdf")
                )
            },
            onEmitAllPdfsClick = {
                batchSaverLauncher.launch()
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
                    secoesAbertas = state.secoesAbertas,
                    sectionErrors = state.sectionErrors,
                    onSelectSection = { onIntent(ReportCreatorIntent.OnSectionChange(it)) },
                    onSelectError = { onIntent(ReportCreatorIntent.OnTabChange(RightPanelTab.ERROS)) },
                    onIntent = onIntent,
                    onFocusRoot = { rootFocusRequester.requestFocus() },
                    modifier = Modifier.width(sidebarWidth).fillMaxHeight()
                )
                ResizingDivider { delta ->
                    sidebarWidth = (sidebarWidth + delta.dp).coerceIn(minSideBarWidth, maxSideBarWidth)
                }

                val remainingWidth = totalWidth - sidebarWidth - 6.dp

                Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val editorModifier = if (state.abaDireitaAtiva != null) {
                        Modifier.width(remainingWidth * editorPanelRatio)
                    } else {
                        Modifier.weight(1f)
                    }

                    Box(
                        modifier = editorModifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        if (currentSection != null) {
                            SectionEditorPanel(
                                section = currentSection,
                                reportData = state.currentReport ?: ReportData(),
                                pdfItems = state.pdfItems,
                                activePdfIndex = state.activePdfIndex,
                                onSelectPdfItem = { onIntent(ReportCreatorIntent.OnSelectPdfItem(it)) },
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
                    if (state.abaDireitaAtiva != null) {
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.abaDireitaAtiva.text,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (state.abaDireitaAtiva == RightPanelTab.VERSOES && state.versions.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Text(
                                            text = "${state.versions.size} versões",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                                        PdfViewer(
                                            pdfPath = state.pdfPath,
                                            cachedBytes = state.pdf[state.pdfPath],
                                            onBytesLoaded = { path, bytes ->
                                                onIntent(ReportCreatorIntent.OnPdfLoaded(path, bytes))
                                            }
                                        )
                                    }

                                    RightPanelTab.VERSOES -> {
                                        VersionsViewer(
                                            versions = state.versions,
                                            onRestoreVersion = { onIntent(ReportCreatorIntent.OnRestoreVersion(it)) },
                                            onDeleteVersion = { onIntent(ReportCreatorIntent.OnDeleteVersion(it)) },
                                            onRenameVersion = { id, nome ->
                                                onIntent(ReportCreatorIntent.OnRenameVersion(id, nome))
                                            },
                                            onFocusRoot = { rootFocusRequester.requestFocus() }
                                        )
                                    }

                                    RightPanelTab.ERROS -> {
                                        ErrorsViewer(
                                            errors = state.sectionErrors,
                                            onSelectSection = { onIntent(ReportCreatorIntent.OnSectionChange(it)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        RightPanelTab.entries.forEach { tab ->
                            val selected = state.abaDireitaAtiva == tab
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Left),
                                tooltip = { PlainTooltip { Text(tab.text) } },
                                state = rememberTooltipState(),
                            ) {
                                FilledIconToggleButton(
                                    checked = selected,
                                    onCheckedChange = {
                                        val newTab = if (selected) null else tab
                                        onIntent(ReportCreatorIntent.OnTabChange(newTab))
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    if (tab == RightPanelTab.ERROS) {
                                        BadgedBox(
                                            badge = {
                                                if (state.sectionErrors.isNotEmpty()) {
                                                    Badge {
                                                        Text(
                                                            text = state.sectionErrors.size.toString(),
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(tab.res),
                                                contentDescription = tab.name,
                                            )
                                        }
                                    } else {
                                        Icon(
                                            painter = painterResource(tab.res),
                                            contentDescription = tab.name,
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
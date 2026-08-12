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
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.Flow
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.change_history
import metrodoc.shared.generated.resources.original_pdf
import metrodoc.shared.generated.resources.preview
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocLoadingDialog
import org.senai.metrodoc.common.util.PdfGenerator.Companion.savePdf
import org.senai.metrodoc.features.report.model.Imagem
import org.senai.metrodoc.features.report.model.ReportData
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
    VERSIONS("Versões", Res.drawable.change_history)
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
        scope.savePdf(
            file = file,
            getpdfBytes = {pdfBytesToSave},
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
            onDismiss = { onIntent(ReportCreatorIntent.OnBackDismissed) },
            onSave = { onIntent(ReportCreatorIntent.OnSaveProject) }
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
                keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape
            }
    ) {
        TopToolbar(
            title = state.reportName,
            exportEnabled = state.canExport,
            savedState = state.reportSaveState,
            onUpdateTitle = {
                onIntent(ReportCreatorIntent.OnReportNameChanged(it))
            },
            onBackClick = { onIntent(ReportCreatorIntent.OnBackClicked) },
            onSave = { onIntent(ReportCreatorIntent.OnSaveProject) },
            onEmitReportClick = {
                saverLauncher.launch(
                    suggestedName = "${state.reportName}.pdf",
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
                    isReportDataValid = state.currentReport?.isValid ?: false,
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

                                if (state.abaDireitaAtiva == RightPanelTab.VERSIONS && state.versions.isNotEmpty()) {
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

                                    RightPanelTab.VERSIONS -> {
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
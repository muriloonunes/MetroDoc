package org.senai.metrodoc.features.welcome.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.add
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.theme.metroDocDefaultScrollbarStyle
import org.senai.metrodoc.common.util.toDateTimeString
import org.senai.metrodoc.features.welcome.presentation.WelcomeEffect
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenIntent
import java.awt.Desktop
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun WelcomeContent(
    projetosRecentes: List<ProjectDto>,
    carregandoProjetos: Boolean,
    onIntent: (WelcomeScreenIntent) -> Unit,
    onNavigateToRelatoryCreator: (Long?, String, String) -> Unit,
    effect: Flow<WelcomeEffect>,
) {
    val pickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("pdf"),
        mode = FileKitMode.Single
    ) { file ->
        file?.let {
            onIntent(
                WelcomeScreenIntent.OnFileSelected(
                    path = it.path,
                    name = it.name
                )
            )
        }
    }

    val scope = rememberCoroutineScope()
    var pdfBytesToSave by remember { mutableStateOf<ByteArray?>(null) }
    var projectIdToExport by remember { mutableStateOf<Long?>(null) }
    var isDraggingOver by remember { mutableStateOf(false) }

    val saverLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        if (file != null) {
            scope.launch {
                if (pdfBytesToSave == null) {

                    onIntent(WelcomeScreenIntent.OnGeneratePdf(projectIdToExport ?: return@launch))
                }
                val bytes = snapshotFlow { pdfBytesToSave }
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

                pdfBytesToSave = null
                projectIdToExport = null
            }
        } else {
            projectIdToExport = null
        }
    }
    LaunchedEffect(effect) {
        effect.collect { welcomeEffect ->
            when (welcomeEffect) {
                is WelcomeEffect.TriggerFilePicker -> {
                    pickerLauncher.launch()
                }

                is WelcomeEffect.NavigateToRelatoryCreator -> {
                    onNavigateToRelatoryCreator(
                        welcomeEffect.reportId,
                        welcomeEffect.path,
                        welcomeEffect.pdfName
                    )
                }

                is WelcomeEffect.OnPdfGenerated -> {
                    pdfBytesToSave = welcomeEffect.bytes
                }
            }
        }
    }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                isDraggingOver = true
                super.onEntered(event)
            }

            override fun onExited(event: DragAndDropEvent) {
                isDraggingOver = false
                super.onExited(event)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDraggingOver = false
                val awtTransferable = event.awtTransferable
                if (awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val data = awtTransferable.getTransferData(DataFlavor.javaFileListFlavor)
                    if (data !is List<*>) return false

                    val files = data.filterIsInstance<File>()
                    if (files.size > 1) return false

                    val pdfFile = files.firstOrNull { it.extension.lowercase() == "pdf" }
                    if (pdfFile != null) {
                        onIntent(
                            WelcomeScreenIntent.OnFileSelected(
                                path = pdfFile.absolutePath,
                                name = pdfFile.name
                            )
                        )
                        return true
                    }
                }
                return false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isHovered) 1.08f else 1.0f,
                    label = "buttonScale"
                )

                Text(
                    text = "Bem-vindo!",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Text(
                    text = "Selecione um PDF para iniciar a estruturação do relatório",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                MediumFloatingActionButton(
                    onClick = { onIntent(WelcomeScreenIntent.OnOpenFileButtonClicked) },
                    interactionSource = interactionSource,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .scale(scale)
                        .dashedBorder(
                            width = Dp.Hairline,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp),
                            on = if (isHovered) 5.dp else 5.dp,
                            off = if (isHovered) 0.dp else 10.dp
                        )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = "Abrir PDF"
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Projetos recentes",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (projetosRecentes.isNotEmpty()) {
                        TextButton(onClick = {
                            onIntent(WelcomeScreenIntent.OnRequestDeleteAllProjects)
                        }) {
                            Text("Limpar recentes", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxSize()) {
                    val gridState = rememberLazyGridState()
                    if (carregandoProjetos) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (projetosRecentes.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 320.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            state = gridState,
                            modifier = Modifier.fillMaxSize().padding(end = 12.dp)
                        ) {
                            items(projetosRecentes) { projeto ->
                                val data = projeto.modificadoEm.toDateTimeString()
                                RecentProjectCard(
                                    projectName = projeto.nomeProjeto,
                                    lastModified = data,
                                    onOpenProject = {
                                        onIntent(WelcomeScreenIntent.OnProjectSelected(projeto.id))
                                    },
                                    onExportPdf = {
                                        projectIdToExport = projeto.id
                                        saverLauncher.launch(
                                            suggestedName = "${projeto.nomeProjeto}.pdf",
                                            defaultExtension = "pdf",
                                            allowedExtensions = setOf("pdf")
                                        )
                                    },
                                    onDeleteFile = {
                                        onIntent(WelcomeScreenIntent.OnRequestDeleteProject(projeto))
                                    }
                                )
                            }
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState = gridState),
                            style = metroDocDefaultScrollbarStyle(),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    } else {
                        Text(
                            text = "Nenhum projeto recente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                }
            }
        }
        AnimatedVisibility(
            visible = isDraggingOver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.15f))
                        .dashedBorder(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(24.dp),
                            on = 12.dp,
                            off = 8.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.add),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Solte o arquivo PDF aqui",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
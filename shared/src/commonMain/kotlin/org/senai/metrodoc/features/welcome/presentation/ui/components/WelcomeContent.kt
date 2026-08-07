package org.senai.metrodoc.features.welcome.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.Flow
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.add
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.theme.metroDocDefaultScrollbarStyle
import org.senai.metrodoc.features.welcome.presentation.WelcomeEffect
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenIntent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            }
        }
    }

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
                TextButton(onClick = { /* TODO: Limpar historico ou ver todos */ }) {
                    Text("Limpar recentes", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                val gridState = rememberLazyGridState()
                if (carregandoProjetos) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (projetosRecentes.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize().padding(end = 12.dp)
                    ) {
                        items(projetosRecentes) { projeto ->
                            val data = Instant
                                .ofEpochMilli(projeto.modificadoEm)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            RecentProjectCard(
                                projectName = projeto.nomeProjeto,
                                lastModified = data,
                                onOpenProject = {
                                    onIntent(WelcomeScreenIntent.OnProjectSelected(projeto.id))
                                },
                                onExportPdf = {},
                                onDeleteFile = {}
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }

}
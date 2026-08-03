package org.senai.metrodoc.features.welcome.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.export
import metrodoc.shared.generated.resources.file
import metrodoc.shared.generated.resources.more_vert
import org.jetbrains.compose.resources.painterResource

@Composable
fun RecentProjectCard(
    projectName: String,
    lastModified: String,
    onOpenProject: () -> Unit,
    onExportPdf: () -> Unit,
    onRemoveFromRecent: () -> Unit,
    onDeleteFile: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onOpenProject,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        color = if (isHovered) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (isHovered) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.file),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Editado em $lastModified",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedVisibility(
                visible = isHovered || showMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                        tooltip = { PlainTooltip { Text("Exportar PDF diretamente") } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(
                            onClick = onExportPdf,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.export),
                                contentDescription = "Exportar PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.more_vert),
                                contentDescription = "Mais opções",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remover dos recentes", fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onRemoveFromRecent()
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Excluir arquivo do computador",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteFile()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
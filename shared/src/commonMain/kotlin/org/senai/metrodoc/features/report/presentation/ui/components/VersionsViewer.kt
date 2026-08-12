package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.TitleEditorTextField
import org.senai.metrodoc.features.report.model.ProjectVersion

@Composable
fun VersionsViewer(
    versions: List<ProjectVersion>,
    modifier: Modifier = Modifier,
    onRestoreVersion: (Long) -> Unit,
    onDeleteVersion: (Long) -> Unit,
    onRenameVersion: (Long, String) -> Unit,
    onFocusRoot: () -> Unit,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        if (versions.isEmpty()) {
            Text(
                text = "Nenhuma versão salva",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(versions, key = { _, item -> item.id }) { index, version ->
                    val isCurrent = index == 0
                    VersionRowItem(
                        version = version,
                        isCurrent = isCurrent,
                        onRestore = { onRestoreVersion(version.id) },
                        onDelete = { onDeleteVersion(version.id) },
                        onRename = { newTitle -> onRenameVersion(version.id, newTitle) },
                        onFocusRoot = onFocusRoot,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(listState)
            )
        }
    }
}

@Composable
private fun VersionRowItem(
    version: ProjectVersion,
    isCurrent: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onFocusRoot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember(version.id) { mutableStateOf(false) }
    var editedTitle by remember(version.id, version.nomeVersao) {
        mutableStateOf(
            TextFieldValue(
                text = version.nomeVersao,
                selection = TextRange(version.nomeVersao.length)
            )
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isEditing) {
        if (!isEditing) {
            onFocusRoot()
        }
    }

    if (isEditing) {
        VersionInlineEditorRow(
            title = editedTitle,
            onTitleChange = { editedTitle = it },
            onConfirm = {
                val trimmed = editedTitle.text.trim()
                if (trimmed.isNotEmpty() && trimmed != version.nomeVersao) {
                    onRename(trimmed)
                }
                isEditing = false
            },
            onCancel = {
                editedTitle = TextFieldValue(
                    text = version.nomeVersao,
                    selection = TextRange(version.nomeVersao.length)
                )
                isEditing = false
            },
            modifier = modifier
        )
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .hoverable(interactionSource),
            color = if (isHovered) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Versão: ${version.nomeVersao}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isHovered) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = { PlainTooltip { Text("Renomear versão") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.edit),
                                    contentDescription = "Renomear versão",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (isCurrent) {
                    Text(
                        text = "Versão atual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = { PlainTooltip { Text("Restaurar versão") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(
                                onClick = onRestore,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.undo),
                                    contentDescription = "Restaurar versão",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = { PlainTooltip { Text("Excluir versão") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.delete),
                                    contentDescription = "Excluir versão",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionInlineEditorRow(
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp)),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                TitleEditorTextField(
                    title = title,
                    onTitleChange = onTitleChange,
                    focusRequester = focusRequester,
                    onConfirm = onConfirm,
                    onCancel = onCancel
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.close),
                        contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onConfirm,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.confirm),
                        contentDescription = "Confirmar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}



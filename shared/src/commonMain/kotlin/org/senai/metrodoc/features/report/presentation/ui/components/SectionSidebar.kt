package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocAddButton
import org.senai.metrodoc.common.ui.TitleEditorTextField
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ReportCreatorIntent

@Composable
fun SectionSidebar(
    secoes: List<ReportSection>,
    selectedId: String?,
    onSelectSection: (String) -> Unit,
    onIntent: (ReportCreatorIntent) -> Unit,
    onFocusRoot: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var isAddingSection by remember { mutableStateOf(false) }
    var newSectionTitle by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
        )
    }

    LaunchedEffect(isAddingSection) {
        if (isAddingSection) {
            listState.animateScrollToItem(secoes.size)
        } else {
            onFocusRoot()
        }
    }

    Column(
        modifier = modifier
            .padding(4.dp)
    ) {
        Text(
            text = "Estrutura do Relatório",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(secoes, key = { _, s -> s.id }) { index, section ->
                    SectionSidebarTile(
                        section = section,
                        index = index,
                        totalCount = secoes.size,
                        isSelected = selectedId == section.id,
                        onSelect = {
                            if (isAddingSection) {
                                isAddingSection = false
                                newSectionTitle = TextFieldValue("")
                            }
                            onSelectSection(section.id)
                            onFocusRoot()
                        },
                        onIntent = onIntent,
                        onFocusRoot = onFocusRoot,
                        modifier = Modifier.animateItem()
                    )
                }

                if (isAddingSection) {
                    item(key = "inline_add_section_tile") {
                        AddSectionInlineTile(
                            title = newSectionTitle,
                            onTitleChange = { newSectionTitle = it },
                            onConfirm = {
                                val trimmed = newSectionTitle.text.trim()
                                if (trimmed.isNotEmpty()) {
                                    val newSection = ReportSection.Customizada(titulo = trimmed)
                                    onIntent(ReportCreatorIntent.OnAddSection(newSection))
                                    onSelectSection(newSection.id)
                                    isAddingSection = false
                                    newSectionTitle = TextFieldValue("")
                                }
                            },
                            onCancel = {
                                isAddingSection = false
                                newSectionTitle = TextFieldValue("")
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                item {
                    MetroDocAddButton(
                        text = "Adicionar Seção",
                        onClick = {
                            isAddingSection = true
                            newSectionTitle = TextFieldValue("")
                        },
                        modifier = Modifier.fillMaxWidth()
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
private fun SectionInlineEditorTile(
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    placeholderText: String = "Nome da seção",
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .heightIn(min = 50.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (title.text.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
                TitleEditorTextField(
                    title = title,
                    onTitleChange = onTitleChange,
                    focusRequester = focusRequester,
                    onConfirm = onConfirm,
                    onCancel = onCancel
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = "Cancelar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            IconButton(
                onClick = onConfirm,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.confirm),
                    contentDescription = "Confirmar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun AddSectionInlineTile(
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionInlineEditorTile(
        title = title,
        onTitleChange = onTitleChange,
        onConfirm = onConfirm,
        onCancel = onCancel,
        placeholderText = "Nome da nova seção",
        modifier = modifier
    )
}

@Composable
private fun SectionSidebarTile(
    section: ReportSection,
    index: Int,
    totalCount: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onIntent: (ReportCreatorIntent) -> Unit,
    onFocusRoot: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isEditing by remember(section.id) { mutableStateOf(false) }
    var editedTitle by remember(section.id, section.titulo) {
        mutableStateOf(TextFieldValue(section.titulo))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val onEdit = {
        onSelect()
        editedTitle = TextFieldValue(
            text = section.titulo,
            selection = TextRange(section.titulo.length)
        )
        isEditing = true
    }

    if (isEditing && section is ReportSection.Customizada) {
        SectionInlineEditorTile(
            title = editedTitle,
            onTitleChange = { editedTitle = it },
            onConfirm = {
                val trimmed = editedTitle.text.trim()
                if (trimmed.isNotEmpty()) {
                    onIntent(ReportCreatorIntent.OnUpdateSection(section.copy(titulo = trimmed)))
                }
                isEditing = false
                onFocusRoot()
            },
            onCancel = {
                isEditing = false
                onFocusRoot()
            },
            placeholderText = "Nome da seção",
            modifier = modifier
        )
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .hoverable(interactionSource)
                .heightIn(min = 50.dp)
                .then(
                    if (section is ReportSection.Customizada) {
                        Modifier.combinedClickable(
                            interactionSource = interactionSource,
                            onClick = { onSelect() },
                            onDoubleClick = { onEdit() }
                        )
                    } else {
                        Modifier.clickable { onSelect() }
                    }
                ),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = section.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    if (section is ReportSection.Customizada && isHovered) {
                        IconButton(
                            onClick = {
                                onEdit()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.edit),
                                contentDescription = "Editar título",
                            )
                        }
                    }
                    if (index > 3 && section.movivel) {
                        IconButton(
                            onClick = { onIntent(ReportCreatorIntent.OnMoveSection(index, index - 1)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.up),
                                contentDescription = "Subir",
                            )
                        }
                    }
                    if (section.movivel && index < totalCount - 2) {
                        IconButton(
                            onClick = { onIntent(ReportCreatorIntent.OnMoveSection(index, index + 1)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.down),
                                contentDescription = "Descer",
                            )
                        }
                    }
                    if (section.removivel) {
                        IconButton(
                            onClick = { onIntent(ReportCreatorIntent.OnRemoveSection(section.id)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.remove),
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
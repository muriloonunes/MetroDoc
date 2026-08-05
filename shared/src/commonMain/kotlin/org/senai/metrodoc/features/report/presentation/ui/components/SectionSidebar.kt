package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.ui.MetroDocAddButton
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
    var newSectionTitle by remember { mutableStateOf("") }

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
                                newSectionTitle = ""
                            }
                            onSelectSection(section.id)
                            onFocusRoot()
                        },
                        onIntent = onIntent,
                        modifier = Modifier.animateItem()
                    )
                }

                if (isAddingSection) {
                    item(key = "inline_add_section_tile") {
                        AddSectionInlineTile(
                            title = newSectionTitle,
                            onTitleChange = { newSectionTitle = it },
                            onConfirm = {
                                val trimmed = newSectionTitle.trim()
                                if (trimmed.isNotEmpty()) {
                                    val newSection = ReportSection.Customizada(titulo = trimmed)
                                    onIntent(ReportCreatorIntent.OnAddSection(newSection))
                                    onSelectSection(newSection.id)
                                    isAddingSection = false
                                    newSectionTitle = ""
                                }
                            },
                            onCancel = {
                                isAddingSection = false
                                newSectionTitle = ""
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
                            newSectionTitle = ""
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
private fun AddSectionInlineTile(
    title: String,
    onTitleChange: (String) -> Unit,
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
                if (title.isEmpty()) {
                    Text(
                        text = "Nome da nova seção",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.Enter, Key.NumPadEnter -> {
                                        onConfirm()
                                        true
                                    }
                                    Key.Escape -> {
                                        onCancel()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
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
private fun SectionSidebarTile(
    section: ReportSection,
    index: Int,
    totalCount: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onIntent: (ReportCreatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onSelect() }
            .heightIn(min = 50.dp),
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
                if (index > 1 && section.movivel) {
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
                if (section.movivel && index < totalCount - 1) {
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
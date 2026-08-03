package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.down
import metrodoc.shared.generated.resources.remove
import metrodoc.shared.generated.resources.up
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
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

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
                        onSelect = { onSelectSection(section.id) },
                        onIntent = onIntent,
                        modifier = Modifier.animateItem()
                    )
                }

                item {
                    MetroDocAddButton(
                        text = "Adicionar Seção",
                        onClick = {
                            onIntent(ReportCreatorIntent.OnAddSection(ReportSection.Introducao()))
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
                if (index > 1 && section.titulo != "Introdução") {
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
                if (section.titulo != "Introdução" && index < totalCount - 1) {
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
                if (section.titulo != "Introdução") {
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
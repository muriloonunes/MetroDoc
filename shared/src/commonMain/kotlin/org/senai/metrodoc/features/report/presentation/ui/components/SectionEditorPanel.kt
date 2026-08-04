package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.close
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.theme.metroDocDefaultScrollbarStyle
import org.senai.metrodoc.common.ui.MetroDocAddButton
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import org.senai.metrodoc.features.report.presentation.ReportCreatorIntent
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.PaginaIdentificacao
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.TableGridHeader
import org.senai.metrodoc.features.welcome.presentation.ui.components.dialog.components.MeasurementTableRow

@Composable
fun SectionEditorPanel(
    section: ReportSection,
    reportData: ReportData,
    onIntent: (ReportCreatorIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = section.titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        when (section) {
            is ReportSection.Introducao -> {
                IntroducaoSectionEditor(
                    introducao = section,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) }
                )
            }

            is ReportSection.Identificacao -> {
                PaginaIdentificacao(
                    reportData = reportData,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnReportFieldChanged(it)) }
                )
            }

            is ReportSection.ResultadosDimensionais -> {
                ResultadosDimensionaisSectionEditor(
                    section = section,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) },
                    onAddMeasurement = { onIntent(ReportCreatorIntent.OnAddMeasurement(section.id)) }
                )
            }

            is ReportSection.InterpretacaoResultados -> {
                InterpretacaoResultadosSectionEditor(
                    section = section,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) }
                )
            }

            is ReportSection.Conclusao -> {
                ConclusaoSectionEditor(
                    section = section,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) }
                )
            }
        }
    }
}

@Composable
fun IntroducaoSectionEditor(
    introducao: ReportSection.Introducao,
    onDataChanged: (ReportSection.Introducao) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val imageLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        file?.let {
            onDataChanged(introducao.copy(imagePath = it.path))
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetroDocTextField(
                label = "Título do Relatório",
                value = introducao.relatorioTitulo,
                placeholder = "Ex: Análise Dimensional e Tomográfica",
                onValueChange = { onDataChanged(introducao.copy(relatorioTitulo = it)) },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            introducao.textos.forEachIndexed { index, texto ->
                MetroDocTextField(
                    label = texto.titulo,
                    value = texto.texto,
                    singleLine = false,
                    minLines = 3,
                    onValueChange = { novoTexto ->
                        val listaAtualizada = introducao.textos.toMutableList()
                        val itemAtualizado = when (texto) {
                            is ReportSection.Introducao.SubTexto.Objetivo -> texto.copy(texto = novoTexto)
                            is ReportSection.Introducao.SubTexto.EscopoAnalise -> texto.copy(texto = novoTexto)
                            is ReportSection.Introducao.SubTexto.ReferenciaMedicao -> texto.copy(texto = novoTexto)
                            is ReportSection.Introducao.SubTexto.Customizado -> texto.copy(texto = novoTexto)
                        }
                        listaAtualizada[index] = itemAtualizado
                        onDataChanged(introducao.copy(textos = listaAtualizada))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "Foto do Componente",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetroDocAddButton(
                    onClick = { imageLauncher.launch() },
                    text = if (introducao.imagePath.isBlank()) "Selecionar Imagem" else "Alterar Imagem",
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                if (introducao.imagePath.isNotBlank()) {
                    MetroDocOutlinedButton(
                        onClick = { onDataChanged(introducao.copy(imagePath = "")) },
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = "Remover Imagem",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            MetroDocTextField(
                label = "Legenda da Imagem",
                placeholder = "Imagem em medição na MMC",
                value = introducao.imagemLegenda,
                enabled = introducao.imagePath.isNotBlank(),
                isRequired = introducao.imagePath.isNotBlank(),
                onValueChange = { onDataChanged(introducao.copy(imagemLegenda = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            MetroDocTextField(
                label = "Notas / Observações",
                value = introducao.observacoes,
                minLines = 2,
                isRequired = false,
                onValueChange = { onDataChanged(introducao.copy(observacoes = it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            style = metroDocDefaultScrollbarStyle(),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
fun ResultadosDimensionaisSectionEditor(
    section: ReportSection.ResultadosDimensionais,
    onDataChanged: (ReportSection.ResultadosDimensionais) -> Unit,
    onAddMeasurement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tabela de Resultados (${section.measurements.size} características)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            MetroDocOutlinedButton(
                onClick = {
                    onAddMeasurement()
                    coroutineScope.launch {
                        listState.animateScrollToItem(section.measurements.size)
                    }
                },
                enabled = section.measurements.last().nome.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text("+ Adicionar Característica")
            }
        }

        if (section.measurements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = "Nenhuma característica cadastrada para esta seção.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetroDocAddButton(
                    text = "Adicionar Medição",
                    onClick = onAddMeasurement
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TableGridHeader()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(
                            items = section.measurements,
                            key = { index, item -> item.hash }
                        ) { index, measurement ->
                            MeasurementTableRow(
                                measurement = measurement,
                                onMeasurementChanged = { updatedMeasurement ->
                                    val listaAtualizada = section.measurements.toMutableList()
                                    listaAtualizada[index] = updatedMeasurement
                                    onDataChanged(section.copy(measurements = listaAtualizada))
                                }
                            )
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(listState),
                        style = metroDocDefaultScrollbarStyle()
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        MetroDocTextField(
            label = "Resumo da Análise Dimensional / Conclusão Técnica",
            value = section.resumoDimensional,
            placeholder = "Ex: O diâmetro medido apresentou resultado dentro dos limites informados. A cilindricidade foi\n" +
                    "registrada acima do limite superior cadastrado.",
            singleLine = false,
            minLines = 3,
            isRequired = false,
            onValueChange = { novoResumo ->
                onDataChanged(section.copy(resumoDimensional = novoResumo))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InterpretacaoResultadosSectionEditor(
    section: ReportSection.InterpretacaoResultados,
    modifier: Modifier = Modifier,
    onDataChanged: (ReportSection.InterpretacaoResultados) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Digite cada tópico em uma linha separada.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        MetroDocTextField(
            label = "Tópicos",
            value = section.topicos,
            singleLine = false,
            minLines = 6,
            onValueChange = {
                onDataChanged(section.copy(topicos = it))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ConclusaoSectionEditor(
    section: ReportSection.Conclusao,
    modifier: Modifier = Modifier,
    onDataChanged: (ReportSection.Conclusao) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MetroDocTextField(
            label = "Conclusão",
            value = section.conclusao,
            singleLine = false,
            minLines = 4,
            onValueChange = {
                onDataChanged(section.copy(conclusao = it))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
package org.senai.metrodoc.features.report.presentation.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import metrodoc.shared.generated.resources.Res
import metrodoc.shared.generated.resources.close
import metrodoc.shared.generated.resources.confirm
import metrodoc.shared.generated.resources.edit
import org.jetbrains.compose.resources.painterResource
import org.senai.metrodoc.common.theme.metroDocDefaultScrollbarStyle
import org.senai.metrodoc.common.ui.MetroDocAddButton
import org.senai.metrodoc.common.ui.MetroDocOutlinedButton
import org.senai.metrodoc.common.ui.MetroDocOutlinedIconButton
import org.senai.metrodoc.common.ui.MetroDocTextField
import org.senai.metrodoc.features.report.model.Imagem
import org.senai.metrodoc.features.report.model.ReportBlock
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
    onFocusRoot: () -> Unit,
    onIntent: (ReportCreatorIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var editarTitulo by remember(section.hashCode()) { mutableStateOf(false) }

        if (section is ReportSection.Customizada) {
            val focusRequester = remember { FocusRequester() }

            var textFieldValue by remember(section.id, editarTitulo) {
                mutableStateOf(
                    TextFieldValue(
                        text = section.titulo,
                        selection = TextRange(section.titulo.length)
                    )
                )
            }

            LaunchedEffect(editarTitulo) {
                if (editarTitulo) {
                    focusRequester.requestFocus()
                } else {
                    onFocusRoot()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editarTitulo) {
                    MetroDocTextField(
                        label = "",
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        textFieldModifier = Modifier
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { keyEvent ->
                                keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                                    Key.Enter, Key.NumPadEnter -> {
                                        val trimmed = textFieldValue.text.trim()
                                        if (trimmed.isNotEmpty()) {
                                            onIntent(ReportCreatorIntent.OnUpdateSection(section.copy(titulo = trimmed)))
                                        }
                                        editarTitulo = false
                                        true
                                    }

                                    Key.Escape -> {
                                        editarTitulo = false
                                        true
                                    }

                                    else -> false
                                }
                            }
                    )
                    IconButton(
                        onClick = {
                            editarTitulo = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        )
                    }

                    IconButton(
                        onClick = {
                            val trimmed = textFieldValue.text.trim()
                            if (trimmed.isNotEmpty()) {
                                onIntent(ReportCreatorIntent.OnUpdateSection(section.copy(titulo = trimmed)))
                            }
                            editarTitulo = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.confirm),
                            contentDescription = "Confirmar",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        )
                    }
                } else {
                    Text(
                        text = section.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = {
                            editarTitulo = true
                        },
                        modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = "Editar Título da Seção",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                        )
                    }
                }
            }
        } else {
            Text(
                text = section.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        when (section) {
            is ReportSection.Introducao -> {
                IntroducaoSectionEditor(
                    introducao = section,
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) },
                    onOpenEditImage = { imagem -> onIntent(ReportCreatorIntent.OnEditImageClicked(imagem)) }
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

            is ReportSection.Customizada -> {
                CustomizadaSectionEditor(
                    section = section,
                    onEditImageClick = { onIntent(ReportCreatorIntent.OnEditImageClicked(it)) },
                    onDataChanged = { onIntent(ReportCreatorIntent.OnUpdateSection(it)) }
                )
            }
        }
    }
}

@Composable
fun CustomizadaSectionEditor(
    section: ReportSection.Customizada,
    onEditImageClick: (Imagem) -> Unit,
    onDataChanged: (ReportSection.Customizada) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (section.blocos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Essa seção ainda não possui conteúdo. Utilize os botões abaixo para adicionar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                ) {
                    itemsIndexed(section.blocos, key = { _, bloco -> bloco.id }) { index, bloco ->
                        val tituloBloco = when (bloco) {
                            is ReportBlock.Texto -> "Bloco de Texto"
                            is ReportBlock.GaleriaImagem -> "Galeria de Imagens"
                            is ReportBlock.QuebraPagina -> "Quebra de Página"
                        }
                        BlockWrapper(
                            tituloBloco = tituloBloco,
                            index = index,
                            totalCount = section.blocos.size,
                            onMoveUp = {
                                val list = section.blocos.toMutableList()
                                val item = list.removeAt(index)
                                list.add(index - 1, item)
                                onDataChanged(section.copy(blocos = list))
                            },
                            onMoveDown = {
                                val list = section.blocos.toMutableList()
                                val item = list.removeAt(index)
                                list.add(index + 1, item)
                                onDataChanged(section.copy(blocos = list))
                            },
                            onRemove = {
                                val list = section.blocos.toMutableList()
                                list.removeAt(index)
                                onDataChanged(section.copy(blocos = list))
                            },
                            modifier = Modifier.animateItem()
                        ) {
                            when (bloco) {
                                is ReportBlock.Texto -> {
                                    TextoBlocoEditor(
                                        block = bloco,
                                        onUpdate = {
                                            val list = section.blocos.toMutableList()
                                            list[index] = it
                                            onDataChanged(section.copy(blocos = list))
                                        }
                                    )
                                }

                                is ReportBlock.GaleriaImagem -> {
                                    GaleriaImagemBlocoEditor(
                                        block = bloco,
                                        onEdit = {
                                            onEditImageClick(it)
                                        },
                                        onUpdate = {
                                            val list = section.blocos.toMutableList()
                                            list[index] = it
                                            onDataChanged(section.copy(blocos = list))
                                        }
                                    )
                                }

                                is ReportBlock.QuebraPagina -> {

                                }
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    style = metroDocDefaultScrollbarStyle(),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Text(
            text = "Adicionar Conteúdo:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetroDocOutlinedButton(
                onClick = {
                    onDataChanged(section.copy(blocos = section.blocos + ReportBlock.Texto()))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Adicionar Texto")
            }
            MetroDocOutlinedButton(
                onClick = {
                    onDataChanged(section.copy(blocos = section.blocos + ReportBlock.GaleriaImagem()))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Adicionar Imagem")
            }
            MetroDocOutlinedButton(
                onClick = {
                    onDataChanged(section.copy(blocos = section.blocos + ReportBlock.QuebraPagina()))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Adicionar Quebra de Página")
            }
        }
    }
}

@Composable
fun IntroducaoSectionEditor(
    introducao: ReportSection.Introducao,
    onDataChanged: (ReportSection.Introducao) -> Unit,
    onOpenEditImage: (Imagem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val imageLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        file?.let {
            onDataChanged(
                introducao.copy(
                    imagem = introducao.imagem.copy(
                        path = it.path,
                        drawings = emptyList(),
                        canvasSize = Size.Zero
                    )
                )
            )
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(end = 14.dp),
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
            val hasImage = introducao.imagem.path.isNotBlank()
            Text(
                text = "Foto do Componente*",
                style = MaterialTheme.typography.labelLarge,
                color = if (hasImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetroDocAddButton(
                    onClick = { imageLauncher.launch() },
                    borderColor = if (hasImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    text = if (hasImage) "Alterar Imagem" else "Selecionar Imagem",
                    modifier = Modifier.weight(1f)
                )
                if (introducao.imagem.path.isNotBlank()) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                        tooltip = { PlainTooltip { Text("Remover imagem") } },
                        state = rememberTooltipState()
                    ) {
                        MetroDocOutlinedIconButton(
                            onClick = { onDataChanged(introducao.copy(imagem = Imagem(path = ""))) },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.close),
                                contentDescription = "Remover Imagem",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                        tooltip = { PlainTooltip { Text("Editar imagem") } },
                        state = rememberTooltipState()
                    ) {
                        MetroDocOutlinedIconButton(
                            onClick = {
                                onOpenEditImage(introducao.imagem)
                            },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.edit),
                                contentDescription = "Editar Imagem",
                            )
                        }
                    }
                }
            }
            MetroDocTextField(
                label = "Legenda da Imagem",
                placeholder = "Peça em medição na MMC",
                value = introducao.imagem.legenda,
                enabled = introducao.imagem.path.isNotBlank(),
                onValueChange = { onDataChanged(introducao.copy(imagem = introducao.imagem.copy(legenda = it))) },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Informações Extras",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Essas informações são opcionais e serão anexadas no final da tabela",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            introducao.informacoesExtras.forEachIndexed { index, infoExtra ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetroDocTextField(
                        label = "Info Extra ${index + 1}:",
                        value = infoExtra.titulo,
                        placeholder = "Diâmetro",
                        onValueChange = { novoTitulo ->
                            val listaAtualizada = introducao.informacoesExtras.toMutableList()
                            val itemAtualizado = (infoExtra as? ReportSection.Introducao.SubTexto.Customizado)
                                ?.copy(titulo = novoTitulo) ?: ReportSection.Introducao.SubTexto.Customizado(
                                titulo = novoTitulo,
                                texto = infoExtra.texto
                            )
                            listaAtualizada[index] = itemAtualizado
                            onDataChanged(introducao.copy(informacoesExtras = listaAtualizada))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetroDocTextField(
                        label = "Descrição",
                        value = infoExtra.texto,
                        placeholder = "Dentro dos limites",
                        onValueChange = { novoTexto ->
                            val listaAtualizada = introducao.informacoesExtras.toMutableList()
                            val itemAtualizado = (infoExtra as? ReportSection.Introducao.SubTexto.Customizado)
                                ?.copy(texto = novoTexto) ?: ReportSection.Introducao.SubTexto.Customizado(
                                titulo = infoExtra.titulo,
                                texto = novoTexto
                            )
                            listaAtualizada[index] = itemAtualizado
                            onDataChanged(introducao.copy(informacoesExtras = listaAtualizada))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetroDocOutlinedButton(
                        onClick = {
                            val listaAtualizada = introducao.informacoesExtras.toMutableList()
                            listaAtualizada.removeAt(index)
                            onDataChanged(introducao.copy(informacoesExtras = listaAtualizada))
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = "Remover Informação Extra",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            MetroDocAddButton(
                onClick = {
                    onDataChanged(
                        introducao.copy(
                            informacoesExtras = introducao.informacoesExtras + ReportSection.Introducao.SubTexto.Customizado()
                        )
                    )
                },
                text = "Adicionar Informação Extra",
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
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
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
                            key = { _, item -> item.hash }
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
            placeholder = "Ex: O diâmetro medido apresentou resultado dentro dos limites informados. A cilindricidade foi " +
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